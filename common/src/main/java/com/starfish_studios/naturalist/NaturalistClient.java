package com.starfish_studios.naturalist;

import com.starfish_studios.naturalist.client.renderer.*;
import com.starfish_studios.naturalist.core.platform.ClientPlatformHelper;
import com.starfish_studios.naturalist.core.registry.NaturalistEntityTypes;
import com.starfish_studios.naturalist.core.registry.NaturalistRegistry;
import net.minecraft.client.renderer.RenderType;

public class NaturalistClient {
    public static void init() {
        ClientPlatformHelper.setRenderLayer(NaturalistRegistry.CHRYSALIS_BLOCK, RenderType.cutout());
        ClientPlatformHelper.setRenderLayer(NaturalistRegistry.AZURE_FROGLASS, RenderType.translucent());
        ClientPlatformHelper.setRenderLayer(NaturalistRegistry.AZURE_FROGLASS_PANE, RenderType.translucent());
        ClientPlatformHelper.setRenderLayer(NaturalistRegistry.VERDANT_FROGLASS, RenderType.translucent());
        ClientPlatformHelper.setRenderLayer(NaturalistRegistry.VERDANT_FROGLASS_PANE, RenderType.translucent());
        ClientPlatformHelper.setRenderLayer(NaturalistRegistry.CRIMSON_FROGLASS, RenderType.translucent());
        ClientPlatformHelper.setRenderLayer(NaturalistRegistry.CRIMSON_FROGLASS_PANE, RenderType.translucent());
        ClientPlatformHelper.setRenderLayer(NaturalistRegistry.SNAIL_EGGS, RenderType.cutout());

//        ClientPlatformHelper.setRenderLayer(NaturalistRegistry.CATTAIL, RenderType.cutout());
//        ClientPlatformHelper.setRenderLayer(NaturalistRegistry.DUCKWEED_BLOCK, RenderType.cutout());

        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.SNAIL, SnailRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.BEAR, BearRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.BUTTERFLY, ButterflyRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.FIREFLY, FireflyRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.SNAKE, SnakeRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.CORAL_SNAKE, SnakeRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.RATTLESNAKE, SnakeRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.DEER, DeerRenderer::new);

        // BIRDS

        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.BLUEJAY, BirdRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.CARDINAL, BirdRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.CANARY, BirdRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.ROBIN, BirdRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.FINCH, BirdRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.SPARROW, BirdRenderer::new);

        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.CATERPILLAR, CaterpillarRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.RHINO, RhinoRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.LION, LionRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.ELEPHANT, ElephantRenderer::new);
        // ZEBRA renderer removed
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.GIRAFFE, GiraffeRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.HIPPO, HippoRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.VULTURE, VultureRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.BOAR, BoarRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.DRAGONFLY, DragonflyRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.CATFISH, CatfishRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.ALLIGATOR, AlligatorRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.BASS, BassRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.LIZARD, LizardRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.LIZARD_TAIL, LizardTailRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.MOOSE, MooseRenderer::new);
        ClientPlatformHelper.registerEntityRenderers(NaturalistEntityTypes.TORTOISE, TortoiseRenderer::new);
        // DUCK renderer removed
    }
}
