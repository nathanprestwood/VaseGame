package ufpe.cin.nmf2.vasegame.ngsi;

import java.util.List;

public class Entity {
	private static final String ENTITY_TYPE = "GetMe41Game";
	String id;
	String type;
	List<Attributes> attributes;

	public Entity() {
		super();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Attributes> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attributes> attributes) {
		this.attributes = attributes;
	}
}
/* ENTITY MODEL
	{
		"id":"#USER_ID#GAME_ID",
		"type":"GetMe41Game",
		"GameId":{
			"value":"#SOMEVALUE"
			"type":"UUID"
		},
		"gameUsername":{
			"value":"#SOMEVALUE"
			"type":"String"
		},
		"gameType":{
			"value":"#SOMEVALUE"
			"type":"String"
		},
		"gameDuration":{
			"value":"#SOMEVALUE"
			"type":"Long"
		},
		"gameDate":{
			"value":"#SOMEVALUE"
			"type":"Date"
		}
	}
 */
