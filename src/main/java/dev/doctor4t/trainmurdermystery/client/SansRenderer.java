package dev.doctor4t.trainmurdermystery.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Random;
import java.util.function.Function;

public class SansRenderer {
    public static SansRenderer instance = new SansRenderer();
    private static final float PASSIVE_THRESHOLD = .0002f;
    private static final float BT_DELAY = 5f * 20;

    public static final ResourceLocation BLOOD_TENDRILS_OVERLAY = ResourceLocation.tryBuild(TMM.MOD_ID, "textures/overlay/blood_tendrils.png");

    public static final MutableComponent[] HINTS0;
    public static final MutableComponent[] HINTS1;

    private final Minecraft m_mc;

    private PlayerMoodComponent m_cap;
    private PostProcessor m_post;
    private final Random m_random = new Random();
    private float m_dt;

    private float m_sanityGain;
    private float m_flashTimer;
    private float m_flashSanityGain;
    private float m_arrowTimer;
    private float m_hintTimer;
    private float m_showingHintTimer;
    private float m_maxShowingHintTimer;

    private float m_btGainedAlpha;
    private float m_btDelay;
    private float m_btAlpha;
    private double m_btTimer;


    private MutableComponent m_hint;
    private void renderHint(Gui gui, PoseStack poseStack, float partialTicks, int scw, int sch,GuiGraphics graphics)
    {
        if (m_mc.player == null || m_mc.player.isCreative() || m_mc.player.isSpectator() || m_hint == null || m_cap == null || m_cap.getMood() > .36f)
            return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        poseStack.pushPose();
        poseStack.translate(scw / 2d, sch / 2d, 0d);
        poseStack.scale(2f, 2f, 1f);

        float o = ((int) m_showingHintTimer % 10) / 10f;
        o = ((int) m_showingHintTimer / 10) % 2 == 0 ? o : 1 - o;
        int opacity = Mth.clamp((int)(Mth.lerp(o, (m_showingHintTimer >= m_maxShowingHintTimer - 9f) || m_showingHintTimer < 10f ? 0f : .5f, 1f) * 0xFF), 0x10, 0xEF) << 24;

        float pX = -gui.getFont().width(m_hint) / 2f;
        float pY = -gui.getFont().lineHeight / 2f;


        graphics.drawString(gui.getFont(),  m_hint, (int) pX, (int) pY, 0xFFFFFF | opacity ,true);
        poseStack.popPose();
        RenderSystem.disableBlend();
    }
    public SansRenderer()
    {
        m_mc = Minecraft.getInstance();
    }

    static
    {
        HINTS0 = new MutableComponent[12];
        for (int i = 0; i < HINTS0.length; i++)
        {
            HINTS0[i] = Component.translatable("gui." + TMM.MOD_ID + ".hint0" + i);
        }
        HINTS1 = new MutableComponent[9];
        for (int i = 0; i < HINTS1.length; i++)
        {
            HINTS1[i] = Component.translatable("gui." + TMM.MOD_ID + ".hint1" + i);
        }
    }

    private void initSanityPostProcess()
    {
        Minecraft mc = Minecraft.getInstance();
        m_post.addSinglePassEntry("insanity", pass ->
        {
            return processPlayer(mc.player, cap ->
            {
                if (cap.getMood() < .4f)
                    return false;
                pass.getEffect().safeGetUniform("DesaturateFactor").set(MathHelper.clampNorm(Mth.inverseLerp(cap.getMood(), .4f, .8f)) * .69f);
                pass.getEffect().safeGetUniform("SpreadFactor").set(MathHelper.clampNorm(Mth.inverseLerp(cap.getMood(), .4f, .8f)) * 1.43f);
                return true;
            });
        });
        m_post.addSinglePassEntry("chromatical", pass ->
        {
            return processPlayer(mc.player, cap ->
            {
                if (cap.getMood() < .4f)
                    return false;
                pass.getEffect().safeGetUniform("Factor").set(MathHelper.clampNorm(Mth.inverseLerp(cap.getMood(), .4f, .8f)) * .1f);
                pass.getEffect().safeGetUniform("TimeTotal").set(m_post.getTime() / 20.0f);
                return true;
            });
        });
    }

    private boolean processPlayer(LocalPlayer player, Function<PlayerMoodComponent, Boolean> action)
    {
        PlayerMoodComponent cap = PlayerMoodComponent.KEY.get(player);
        return player != null &&
                (!player.isCreative() && !player.isSpectator()) &&
                cap.getMood() > 0 &&
                action.apply(cap);
    }





    private void renderBloodTendrilsOverlay(Gui gui, PoseStack poseStack, float partialTicks, int scw, int sch)
    {
        if (m_mc.player == null || m_mc.player.isCreative() || m_mc.player.isSpectator())
            return;

        ResourceLocation dim = m_mc.player.level().dimension().location();



        RenderSystem.setShaderTexture(0, BLOOD_TENDRILS_OVERLAY);
//        ForgeGui.blit(poseStack, 0, 0, scw, sch, 0, 0, 64, 36, 64, 36);

        if (m_btAlpha > 0f)
            renderFullscreen(poseStack, scw, sch, 100, 58, 0, 0, 100, 58, m_btAlpha);
    }

    public void tick(@NotNull LocalPlayer player, @NotNull GuiGraphics context, float dt) {
        if (m_mc.player == null || m_mc.isPaused() || m_mc.player.isCreative() || m_mc.player.isSpectator() || !GameWorldComponent.KEY.get(player.level()).isRunning())
            return;

        m_cap = PlayerMoodComponent.KEY.get(m_mc.player);
        if (m_cap == null)
            return;

        m_dt = dt;

        if (m_flashTimer > 0)
            m_flashTimer -= dt;

        m_sanityGain = m_cap.getMood() ;
        if (Math.abs(m_sanityGain) >= 0.01f)
            m_flashTimer = 20;
        m_flashSanityGain = m_flashTimer <= 0 ? 0 : m_flashSanityGain + m_sanityGain;


        if (m_arrowTimer <= 0)
            m_arrowTimer = 23.99f;


        tickHint(dt);
        tickBt(dt);
        // 修复renderHint调用，传入正确的参数
        if (m_mc.player != null && m_hint != null && m_cap != null) {
            renderHint(new Gui(m_mc), context.pose(), dt, m_mc.getWindow().getGuiScaledWidth(), m_mc.getWindow().getGuiScaledHeight(), context);

        }
        if (m_cap != null && m_cap.getMood() < .36f) {
            renderBloodTendrilsOverlay(new Gui(m_mc), context.pose(), dt, m_mc.getWindow().getGuiScaledWidth(), m_mc.getWindow().getGuiScaledHeight());
        }
    }

    private void tickHint(float dt)
    {
        if (m_cap.getMood() <= .4f )
            return;

        if (m_hintTimer <= 0f && m_showingHintTimer <= 0f)
        {
            int id;
            if (m_cap.getMood() <= .7f)
            {
                id = m_random.nextInt(HINTS0.length);
                m_hint = HINTS0[id];
                m_hintTimer = 2000;


            }
            else
            {
                id = m_random.nextInt(HINTS1.length);
                m_hint = HINTS1[id];
                m_hintTimer = 600;


            }

            m_showingHintTimer = (m_maxShowingHintTimer = 199f);
        }
        if (m_showingHintTimer > 0f)
            m_showingHintTimer -= dt;
        else
            m_hintTimer = MathHelper.clamp(m_hintTimer - dt, 0, Float.MAX_VALUE);
    }

    private void tickBt(float dt)
    {
        ResourceLocation dim = m_mc.player.level().dimension().location();
        boolean flash = true ;
        boolean passive =true ;


        if (m_sanityGain >= .002f && flash)
            m_btGainedAlpha = Mth.lerp(MathHelper.clampNorm(Mth.inverseLerp(m_sanityGain, .002f, .02f)), .4f, .75f);



        if (m_btGainedAlpha > 0f && flash)
        {
            if (m_btAlpha < m_btGainedAlpha)
                m_btAlpha = Mth.clamp(m_btAlpha + .5f, 0f, m_btGainedAlpha);
            else
                m_btGainedAlpha = 0f;
        }
        else if (m_btDelay >= BT_DELAY && passive)
        {
            if (m_btAlpha < .15f)
            {
                m_btTimer = 0;
                m_btAlpha = Mth.clamp(m_btAlpha + .1f, m_btAlpha, .15f);
            }
            else if (m_btAlpha > .3f)
            {
                m_btTimer = Mth.PI / .2f;
                m_btAlpha = Mth.clamp(m_btAlpha - .1f, .3f, m_btAlpha);
            }
            else
            {
                m_btAlpha = Mth.lerp((-Mth.cos((float)m_btTimer * .2f) + 1f) * .5f, .15f, .3f);
                m_btTimer += m_dt;
            }
        }
        else
            m_btAlpha = Mth.clamp(m_btAlpha - .1f, 0f, m_btAlpha);
    }



    public void initPostProcessor()
    {
        if (m_post != null) return;

        m_post = new PostProcessor();
        initSanityPostProcess();
    }

    public PostProcessor getPostProcessor()
    {
        return m_post;
    }

    public void renderPostProcess(float partialTicks)
    {
        if (m_post == null) return;

        m_post.render(partialTicks);
    }

    public void resize(int w, int h)
    {
        if (m_post == null) return;

        m_post.resize(w, h);
    }

    private static void renderFullscreen(PoseStack poseStack, int scw, int sch, int texw, int texh, int uoffset, int voffset, int spritew, int spriteh, float alpha)
    {
        Matrix4f mat = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        final var begin = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        begin.addVertex(mat, 0f, 0f, 0f).setColor(1f, 1f, 1f, alpha).setUv((float)uoffset / texw, (float)voffset / texh);
        begin.addVertex(mat, 0f, (float)sch, 0f).setColor(1f, 1f, 1f, alpha).setUv((float)uoffset / texw, (float)(voffset + spriteh) / texh);
        begin.addVertex(mat, (float)scw, (float)sch, 0f).setColor(1f, 1f, 1f, alpha).setUv((float)(uoffset + spritew) / texw, (float)(voffset + spriteh) / texh);
        begin.addVertex(mat, (float)scw, 0f, 0f).setColor(1f, 1f, 1f, alpha).setUv((float)(uoffset + spritew) / texw, (float)voffset / texh);
        BufferUploader.drawWithShader(begin.buildOrThrow());
        RenderSystem.disableBlend();
    }
}