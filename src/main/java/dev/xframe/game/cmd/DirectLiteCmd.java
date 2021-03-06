package dev.xframe.game.cmd;

import com.google.protobuf.MessageLite;

import dev.xframe.game.player.Player;
import dev.xframe.net.codec.IMessage;
import dev.xframe.utils.LiteParser;

public abstract class DirectLiteCmd<T extends Player, L extends MessageLite> extends DirectCommand<T> {

	private LiteParser parser = new LiteParser(this.getClass(), DirectLiteCmd.class);
	
	@Override
	public final void exec(T player, IMessage req) throws Exception {
		exec(player, parser.<L>parse(req.getBody()));
	}
	
	public abstract void exec(T player, L req) throws Exception;

}
