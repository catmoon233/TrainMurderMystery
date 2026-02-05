package dev.doctor4t.trainmurdermystery.mixin.whitelist;

import dev.doctor4t.trainmurdermystery.mod_whitelist.client.ModWhitelistClient;
import dev.doctor4t.trainmurdermystery.mod_whitelist.common.network.IPacketWithMOD_IDs;
import dev.doctor4t.trainmurdermystery.mod_whitelist.common.utils.MWLogger;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientIntentionPacket.class)
public class ClientIntentionPacketMixin implements IPacketWithMOD_IDs {
	@Shadow @Final
	private ClientIntent intention;

	@Unique @Nullable
	private List<String> modWhitelist$MOD_IDs = null;

	@Override @Nullable
	public List<String> getMOD_IDs() {
		return this.modWhitelist$MOD_IDs;
	}

	@Override
	public void setMOD_IDs(@Nullable List<String> MOD_IDs) {
		this.modWhitelist$MOD_IDs = MOD_IDs;
	}

//	@Inject(method = "<init>(ILjava/lang/String;ILnet/minecraft/network/protocol/handshake/ClientIntent;)V", at = @At(value = "TAIL"))
//	private void getMOD_IDsFromInit(int protocolVersion, String hostName, int port, ClientIntent intention, CallbackInfo ci) {
//		if(intention.equals(ClientIntent.LOGIN)) {
//			this.modWhitelist$MOD_IDs = ModWhitelistClient.mods;
//		}
//	}
//
//	@Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "TAIL"))
//	private void getMOD_IDsFromNetwork(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
//		if(this.intention.equals(ClientIntent.LOGIN)) {
//			try {
//				this.modWhitelist$MOD_IDs = friendlyByteBuf.readList(FriendlyByteBuf::readUtf);
//			} catch (DecoderException e) {
//				MWLogger.LOGGER.warn("Decoder exception occurs when parsing ClientIntentionPacket: ", e);
//			}
//		}
//	}
//
//	@Inject(method = "write", at = @At(value = "TAIL"))
//	private void writeMOD_IDsToNetwork(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
//		if(this.modWhitelist$MOD_IDs != null) {
//			friendlyByteBuf.writeCollection(this.modWhitelist$MOD_IDs, FriendlyByteBuf::writeUtf);
//		}
//	}
}
