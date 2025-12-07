package dev.doctor4t.trainmurdermystery.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.PostEffectProcessor;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PostProcessor
{
//	private float m_time;
//	private final Framebuffer m_swapBuffer;
//	private final Matrix4f m_orthoMat = new Matrix4f();
//	public final List<PostPassEntry> passEntries = new ArrayList<>();
//
//	public PostProcessor()
//	{
//		MinecraftClient mc = MinecraftClient.getInstance();
//		m_swapBuffer = new Framebuffer(mc.getFramebuffer().textureWidth, mc.getFramebuffer().textureHeight, true, MinecraftClient.IS_SYSTEM_MAC);
//		m_swapBuffer.setClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//		updateOrthoMatrix();
//	}
//
//	public float getTime()
//	{
//		return m_time;
//	}
//
//	public PostPassEntry addPassEntry(String in, String out, Function<PostEffectPass, Boolean> inProcessor, Function<PostEffectProcessor, Boolean> outProcessor)
//	{
//		MinecraftClient mc = MinecraftClient.getInstance();
//		PostEffectProcessor inPass;
//		PostEffectProcessor outPass;
//		try
//		{
//			inPass = new PostEffectPass(mc.getResourceManager(), in, mc.getFramebuffer(), m_swapBuffer);
//			inPass.setOrthoMatrix(m_orthoMat);
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//			return null;
//		}
//		try
//		{
//
//			outPass = new PostEffectPass(mc.getResourceManager(), out, m_swapBuffer, mc.getFramebuffer());
//			outPass.setOrthoMatrix(m_orthoMat);
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//			return null;
//		}
//		PostPassEntry entry = new PostPassEntry(inPass, outPass, inProcessor, outProcessor);
//		passEntries.add(entry);
//		return entry;
//	}
//
//	public PostPassEntry addSinglePassEntry(String in, Function<PostEffectPass, Boolean> inProcessor)
//	{
//		return addPassEntry(in, "blit", inProcessor, null);
//	}
//
//	public void render(float partialTicks)
//	{
//		m_time += partialTicks;
//
//		MinecraftClient mc = MinecraftClient.getInstance();
//		if (mc.player == null || mc.player.isCreative() || mc.player.isSpectator() || !TMMClient.shouldDisableHudAndDebug())
//			return;
//
//		for (PostPassEntry entry : passEntries)
//		{
//			if (entry.getInPass() == null || entry.getOutPass() == null ||
//					entry.getInProcessor() != null && !entry.getInProcessor().apply(entry) ||
//					entry.getOutProcessor() != null && !entry.getOutProcessor().apply(entry))
//				continue;
//
//			entry.getInPass().process(partialTicks);
//			entry.getOutPass().process(partialTicks);
//		}
//	}
//
//	public void updateOrthoMatrix()
//	{
//		MinecraftClient mc = MinecraftClient.getInstance();
//		m_orthoMat.setOrtho(0.0f, (float)mc.getFramebuffer().textureWidth, 0.0f, (float)mc.getFramebuffer().textureHeight, .1f, 1000.0f);
//		for (PostPassEntry entry : passEntries)
//		{
//			entry.getInPass().setOrthoMatrix(m_orthoMat);
//			entry.getOutPass().setOrthoMatrix(m_orthoMat);
//		}
//	}
//
//	public void resize(int w, int h)
//	{
//		if (m_swapBuffer != null)
//			m_swapBuffer.resize(w, h, MinecraftClient.IS_SYSTEM_MAC);
//		updateOrthoMatrix();
//	}
//
//	public class PostPassEntry
//	{
//		private PostPass m_in;
//		private PostPass m_out;
//		private Function<PostPassEntry, Boolean> m_inProcessor;
//		private Function<PostPassEntry, Boolean> m_outProcessor;
//
//		public PostPassEntry(PostPass in, PostPass out, Function<PostPass, Boolean> inProcessor, Function<PostPass, Boolean> outProcessor)
//		{
//			m_in = in;
//			m_out = out;
//			m_inProcessor = (Function<PostPassEntry, Boolean>) inProcessor;
//			m_outProcessor = (Function<PostPassEntry, Boolean>) outProcessor;
//		}
//
//		public PostPass getInPass()
//		{
//			return m_in;
//		}
//
//		public PostPass getOutPass()
//		{
//			return m_out;
//		}
//
//		public Function<PostPassEntry, Boolean> getInProcessor()
//		{
//			return m_inProcessor;
//		}
//
//		public Function<PostPassEntry, Boolean> getOutProcessor()
//		{
//			return m_outProcessor;
//		}
//	}
}