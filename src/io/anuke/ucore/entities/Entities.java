package io.anuke.ucore.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import io.anuke.ucore.core.Core;
import io.anuke.ucore.function.Consumer;
import io.anuke.ucore.function.Predicate;
import io.anuke.ucore.util.QuadTree;

public class Entities{
	private static final EntityGroup<Entity> defaultGroup;
	private static final Array<EntityGroup<?>> groupArray = new Array<>();
	private static final IntMap<EntityGroup<?>> groups = new IntMap<>();

	private static final EntityCollisions collisions = new EntityCollisions();
	private static final Array<SolidEntity> array = new Array<>();
	private static final Rectangle viewport = new Rectangle();
	
	public static final int maxLeafObjects = 4;
	
	static{
		defaultGroup = addGroup(Entity.class);
	}

	public static EntityCollisions collisions(){
		return collisions;
	}

	public static void initPhysics(float x, float y, float w, float h){
		for(EntityGroup group : groupArray){
			if(group.useTree)
				group.setTree(x, y, w, h);
		}
	}

	public static void initPhysics(){
		initPhysics(0, 0, 0, 0);
	}

	public static void resizeTree(float x, float y, float w, float h){
		initPhysics(x, y, w, h);
	}

	public static void getNearby(EntityGroup<?> group, Rectangle rect, Consumer<SolidEntity> out){
		if(!group.useTree) throw new RuntimeException("This group does not support quadtrees! Enable quadtrees when creating it.");
		group.tree().getIntersect(out, rect);
	}

	public static Array<SolidEntity> getNearby(EntityGroup<?> group, Rectangle rect){
		array.clear();
		getNearby(group, rect, array::add);
		return array;
	}

	public static void getNearby(float x, float y, float size, Consumer<SolidEntity> out){
		getNearby(defaultGroup(), Rectangle.tmp.setSize(size).setCenter(x, y), out);
	}
	
	public static void getNearby(EntityGroup<?> group, float x, float y, float size, Consumer<SolidEntity> out){
		getNearby(group, Rectangle.tmp.setSize(size).setCenter(x, y), out);
	}
	
	public static Array<SolidEntity> getNearby(float x, float y, float size){
		return getNearby(defaultGroup(), Rectangle.tmp.setSize(size).setCenter(x, y));
	}

	public static Array<SolidEntity> getNearby(EntityGroup<?> group, float x, float y, float size){
		return getNearby(group, Rectangle.tmp.setSize(size).setCenter(x, y));
	}

	public static SolidEntity getClosest(EntityGroup<?> group, float x, float y, float range, Predicate<Entity> pred){
		SolidEntity closest = null;
		float cdist = 0f;
		Array<SolidEntity> entities = getNearby(group, x, y, range * 2f);
		for(int i = 0; i < entities.size; i ++){
			SolidEntity e = entities.get(i);
			if(!pred.test(e))
				continue;

			float dist = Vector2.dst(e.x, e.y, x, y);
			if(dist < range)
				if(closest == null || dist < cdist){
					closest = e;
					cdist = dist;
				}
		}

		return closest;
	}

	public static void clear(){
		for(EntityGroup group : groupArray){
			group.clear();
		}
	}
	
	public static Iterable<Entity> all(){
		return defaultGroup.all();
	}

	public static EntityGroup<?> getGroup(int id){
		return groups.get(id);
	}

	public static Iterable<EntityGroup<?>> getAllGroups(){
		return groups.values();
	}
	
	public static EntityGroup<Entity> defaultGroup(){
		return defaultGroup;
	}
	
	public static <T extends Entity> EntityGroup<T> addGroup(Class<T> type){
		return addGroup(type, true);
	}
	
	public static <T extends Entity> EntityGroup<T> addGroup(Class<T> type, boolean useTree){
		EntityGroup<T> group = new EntityGroup<>(type, useTree);
		groups.put(group.getID(), group);
		groupArray.add(group);
		return group;
	}

	private static void updatePhysics(EntityGroup<?> group){
		
		QuadTree<SolidEntity> tree = group.tree();
		
		tree.clear();

		for(Entity entity : group.all()){
			if(entity instanceof SolidEntity){
				tree.insert((SolidEntity) entity);
			}
		}
	}
	
	public static void collideGroups(EntityGroup<?> groupa, EntityGroup<?> groupb){
		collisions().collideGroups(groupa, groupb);
	}
	
	public static void draw(){
		draw(defaultGroup);
	}

	public static void draw(EntityGroup<?> group){
		OrthographicCamera cam = Core.camera;
		viewport.set(cam.position.x - cam.viewportWidth / 2 * cam.zoom, cam.position.y - cam.viewportHeight / 2 * cam.zoom, cam.viewportWidth * cam.zoom, cam.viewportHeight * cam.zoom);

		for(Entity e : group.all()){
			Rectangle.tmp2.setSize(e.drawSize()).setCenter(e.x, e.y);

			if(Rectangle.tmp2.overlaps(viewport))
				e.draw();
		}

		for(Entity e : group.all()){
			Rectangle.tmp2.setSize(e.drawSize()).setCenter(e.x, e.y);

			if(Rectangle.tmp2.overlaps(viewport))
				e.drawOver();
		}
	}
	
	public static void update(){
		update(defaultGroup());
		collideGroups(defaultGroup(), defaultGroup());
	}

	public static void update(EntityGroup<?> group){
		if(group.useTree){
			updatePhysics(group);
		}
		
		for(Entity e : group.all()){
			e.update();
		}
		
		group.updateRemovals();
	}
}
