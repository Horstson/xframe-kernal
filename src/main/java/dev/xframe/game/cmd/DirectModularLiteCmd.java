package dev.xframe.game.cmd;

import com.google.protobuf.MessageLite;

import dev.xframe.game.player.Player;
import dev.xframe.net.codec.IMessage;
import dev.xframe.utils.LiteParser;

public abstract class DirectModularLiteCmd<T extends Player, V, L extends MessageLite> extends DirectModularCmd<T, V> {

	private LiteParser parser = new LiteParser(this.getClass(), DirectModularLiteCmd.class);
	
	@Override
	public final void exec(T player, V module, IMessage req) throws Exception {
		exec(player, module, parser.<L>parse(req.getBody()));
	}
	
	public abstract void exec(T player,  V module, L req) throws Exception;

}
