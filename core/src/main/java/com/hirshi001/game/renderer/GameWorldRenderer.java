package com.hirshi001.game.renderer;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hirshi001.game.gameworld.ClientGameWorld;
import com.hirshi001.game.gameworld.GameWorld;
import com.hirshi001.game.gameworld.character.*;
import com.hirshi001.game.gameworld.character.Character;
import com.hirshi001.game.gameworld.entity.Entity;
import com.hirshi001.game.gameworld.fire.Fireball;
import com.hirshi001.game.gameworld.ground.EarthRock;
import com.hirshi001.game.gameworld.water.Water;
import com.hirshi001.game.util.UUID;

import java.util.HashMap;
import java.util.Map;

public class GameWorldRenderer extends InputAdapter {

    interface EntityRendererSupplier<T extends Entity> {
        EntityRenderer<T> get(T entity, AssetManager assetManager);
    }

    static Map<Class<?>, EntityRendererSupplier<?>> classToEntityRenderers;

    static <T extends Entity> void addEntityRenderer(Class<T> clazz, EntityRendererSupplier<? extends T> supplier) {
        classToEntityRenderers.put(clazz, supplier);
    }

    @SuppressWarnings("unchecked")
    static <T extends Entity> EntityRenderer<T> getEntityRenderer(T entity, AssetManager assetManager) {
        return ((EntityRendererSupplier<T>) classToEntityRenderers.get(entity.getClass())).get(entity, assetManager);
    }

    static {
        classToEntityRenderers = new HashMap<>();
        addEntityRenderer(EarthCharacter.class, CharacterRenderer::new);
        addEntityRenderer(WaterCharacter.class, CharacterRenderer::new);
        addEntityRenderer(AirCharacter.class, CharacterRenderer::new);
        addEntityRenderer(FireCharacter.class, CharacterRenderer::new);
        addEntityRenderer(EarthRock.class, EarthRockRenderer::new);
        addEntityRenderer(Fireball.class, FireballRenderer::new);
        addEntityRenderer(Water.class, WaterRenderer::new);
    }

    GameWorld gameWorld;

    Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    OrthographicCamera camera;
    Viewport viewport;
    Texture ground = new Texture("game/other/Ground.png");

    Map<UUID, EntityRenderer<?>> entityRenderers;

    SpriteBatch batch = new SpriteBatch();
    AssetManager assetManager = new AssetManager();

    public GameWorldRenderer(GameWorld gameWorld) {
        this.gameWorld = gameWorld;

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(60, 40, camera);
        entityRenderers = new HashMap<>();

        CharacterRenderer.RequestAssets(assetManager);

        assetManager.finishLoading();

    }

    public void render(float delta) {
        entityRenderers.entrySet().removeIf(entry -> !gameWorld.gameObjects.containsKey(entry.getKey()));

        ScreenUtils.clear(Color.WHITE);

        ClientGameWorld clientGameWorld = (ClientGameWorld) gameWorld;

        Vector2 centerPosition = new Vector2();
        for(Character character:clientGameWorld.getCharacters()) {
            if(character.dead)
                continue;
            centerPosition.add(character.getBody().getPosition());
        }
        centerPosition.scl(1f / clientGameWorld.getCharacters().length);
        centerPosition.interpolate(clientGameWorld.clientCharacter.getBody().getPosition(), 0.3F, Interpolation.linear);

        float newX = Interpolation.linear.apply(camera.position.x, centerPosition.x, 0.2F);
        float newY = Interpolation.linear.apply(camera.position.y, centerPosition.y, 0.2F);

        camera.position.set(newX, newY, 0);

        float maxX = Float.MIN_VALUE, minX = Float.MAX_VALUE, maxY = Float.MIN_VALUE, minY = Float.MAX_VALUE;
        for(Character character:clientGameWorld.getCharacters()) {
            Vector2 position = character.getBody().getPosition();
            if(position.x > maxX) {
                maxX = position.x;
            }
            if(position.x < minX) {
                minX = position.x;
            }
            if(position.y > maxY) {
                maxY = position.y;
            }
            if(position.y < minY) {
                minY = position.y;
            }
        }

        // update zoom such that minX, maxX, minY, maxY are all visible
        float zoomX = 200 / (maxX - minX);
        float zoomY = 100 / (maxY - minY);
        float zoom = Math.min(zoomX, zoomY);
        // viewport.setWorldSize(200 / zoom, 100 / zoom);
       //  camera.zoom = zoom;


        camera.update();
        viewport.apply(false);

        // debugRenderer.render(gameWorld.world, camera.combined);



        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for(Entity entity:gameWorld.gameObjects.values()) {
            EntityRenderer<?> renderer = entityRenderers.get(entity.getUUID());
            if(renderer == null) {
                renderer = getEntityRenderer(entity, assetManager);
                entityRenderers.put(entity.getUUID(), renderer);
            }
            renderer.tick(delta);
            renderer.render(batch);
        }

        batch.draw(ground, -30, -30, 60, 20);

        batch.end();


    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }

}
