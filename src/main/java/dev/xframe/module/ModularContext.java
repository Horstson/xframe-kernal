package dev.xframe.module;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import dev.xframe.inject.Bean;
import dev.xframe.inject.Inject;
import dev.xframe.inject.beans.BeanBinder;
import dev.xframe.inject.beans.BeanDefiner;
import dev.xframe.inject.beans.BeanIndexing;
import dev.xframe.inject.beans.BeanPretreater;
import dev.xframe.inject.beans.BeanRegistrator;
import dev.xframe.inject.beans.Injector;
import dev.xframe.inject.code.Codes;
import dev.xframe.inject.code.SyntheticBuilder;
import dev.xframe.module.beans.DeclaredBinder;
import dev.xframe.module.beans.ModularBinder;
import dev.xframe.module.beans.ModularIndexes;
import dev.xframe.module.beans.ModularListener;
import dev.xframe.module.beans.ModuleContainer;
import dev.xframe.utils.XLambda;
import dev.xframe.utils.XReflection;

@Bean
public class ModularContext implements ModularListener {
	
	@Inject
	private BeanRegistrator registrator;
	@Inject
	private BeanIndexing gIndexing;
	@Inject
	private BeanDefiner gDefiner;
	
	private ModularIndexes indexes;
	
	public void initialize(Class<?> assembleClass) {
		indexes = new ModularIndexes(gIndexing);
		registrator.regist(BeanBinder.instanced(this, ModularContext.class));
		registrator.regist(BeanBinder.instanced(indexes, ModularIndexes.class));
		//assembleClass作为第一个Bean记录
		indexes.regist(new DeclaredBinder(assembleClass, Injector.of(assembleClass, indexes)));
		pretreatModules().forEach(c->indexes.regist(buildBinder(c, indexes)));
		indexes.integrate();
	}
	
	public ModuleContainer initContainer(ModuleContainer mc, Object assemble) {
		mc.setup(gDefiner, indexes, this);
		int index = indexes.getIndex(assemble.getClass());
		//为assembleClass赋值 @see initialize() 
		mc.setBean(index, assemble);
		mc.integrate(indexes.getBinder(index));
		return mc;
	}
	
	public ModularBinder getBinder(Class<?> clazz) {
		return (ModularBinder) indexes.getBinder(indexes.indexOf(clazz));
	}
	
	public Injector newInjector(Class<?> c) {
		return Injector.of(c, indexes);
	}

	static Class<?> buildAgentClass(Class<?> c) {
        ModularAgent an = c.getAnnotation(ModularAgent.class);
        return SyntheticBuilder.buildClass(c, an.invokable(), an.ignoreError(), an.boolByTrue());
	}
	
	private BeanBinder buildBinder(Class<?> c, BeanIndexing indexing) {
		if(c.isAnnotationPresent(ModularAgent.class)) {
			return new AgentBinder(c);
		}
		return new ModularBinder(c, Injector.of(c, indexing));
	}

	private List<Class<?>> pretreatModules() {
		return new BeanPretreater(Codes.getDeclaredClasses()).filter(isModularClass()).pretreat(annoComparator()).collect();
	}
	private Predicate<Class<?>> isModularClass() {
		return c -> isModularClass(c);
	}
	private static boolean isModularClass(Class<?> clazz) {
		return isModule(clazz) || isComponent(clazz) || isAgent(clazz);
	}
	private static boolean isAgent(Class<?> clazz) {
        return clazz.isInterface() && clazz.isAnnotationPresent(ModularAgent.class);
    }
	private static boolean isComponent(Class<?> clazz) {
        return clazz.isAnnotationPresent(ModularComponent.class) && !clazz.isAnnotationPresent(ModularIgnore.class);
    }
	//由于@Module可继承 需要过滤掉抽象类
	private static boolean isModule(Class<?> clazz) {
        return XReflection.isImplementation(clazz) && clazz.isAnnotationPresent(Module.class) && !clazz.isAnnotationPresent(ModularIgnore.class);
    }
	//作为实现类存在的ModularClass
	private static Class<? extends Annotation>[] annos = new Class[] {ModularAgent.class, ModularComponent.class, Module.class};
    private static int annoOrder(Class<?> c) {
    	for (int i = 0; i < annos.length; i++) {
			if(c.isAnnotationPresent(annos[i])) return i;
		}
    	return annos.length;
    }
    private static Comparator<Class<?>> annoComparator() {//从小到大
    	return (c1, c2)->Integer.compare(annoOrder(c1), annoOrder(c2));
    }
    
    class AgentBinder extends ModularBinder {
        Supplier<Object> factory;
        List<BeanBinder> impls = new LinkedList<>(); 
        public AgentBinder(Class<?> master) {
            super(master, Injector.NIL);
            factory = XLambda.createByConstructor(buildAgentClass(master));
            agents.add(this);
        }
        //append impl留给ModularListener来处理
        protected void integrate(Object bean, BeanDefiner definer) {
        }
        protected BeanBinder conflict(Object keyword, BeanBinder binder) {
            assert keyword instanceof Class;
            assert ((Class<?>) keyword).isAnnotationPresent(ModularAgent.class);
            impls.add(binder);
            return this;
        }
        protected Object newInstance() {
            return factory.get();
        }
    }
    
    private List<AgentBinder> agents = new LinkedList<>();
    @Override
    public void onModuleLoaded(ModuleContainer mc, ModularBinder binder, Object module) {
        for (AgentBinder agent : agents) {
            if(agent.impls.contains(binder)) {
                SyntheticBuilder.append(mc.getBean(agent.getIndex()), module);
            }
        }
    }

    @Override
    public void onModuleUnload(ModuleContainer mc, ModularBinder binder, Object module) {
        for (AgentBinder agent : agents) {
            if(agent.impls.contains(binder)) {
                SyntheticBuilder.remove(mc.getBean(agent.getIndex()), module);
            }
        }
    }

}
