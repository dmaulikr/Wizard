package com.example.wizard1;

import android.util.Log;

/*
 * Fight action type
 */
enum FightAction {
	
	DAMAGE,
	HIGH_DAMAGE,
	HEAL,
	BUFF_ON,
	BUFF_OFF,
	NEW_HP,
//	NEW_MANA,
	NONE;
	
	/*
	 *  strings are needed for debugging or string message 
	 *  sending, may be deleted in future
	 */
	static String[] names = {
		"damage",
		"high_damage",
		"heal",
		"shield_on",
		"new_hp",
		"new_mana",
		"none"
	};
	
	@Override
	public String toString() {
		return names[ this.ordinal() ];
	}
	
	public static FightAction getFromString(String s) {
		for(int i=0; i<names.length; i++) {
			if( s.equals(names[i]) ) 
				return FightAction.values()[ i ];
		}
		return FightAction.NONE;
	}
}

/*
 * contains all needed info that is transfered between devices
 */
public class FightMessage {
	public FightAction action;
	public Shape shape;
	public int param;
	
	public FightMessage(FightAction msgAction) {
		action = msgAction;
		param = -1;
		shape = getShapeFromMessage(this);
	}
	
	public FightMessage(Shape s) {
		shape = s;
		action = getActionFromShape(shape);
		param = -1;
		if( action == FightAction.BUFF_ON ) {
			param = Buff.getBuffFromShape(shape).ordinal();
		}
	}
	
	public FightMessage(int actionIndex, int parameter) {
		action = FightAction.values() [ actionIndex ];
		param = parameter;
		shape = getShapeFromMessage(this);	
	}
	
	public static Shape getShapeFromMessage(FightMessage message) {
		Shape shape;
		switch( message.action ) {
		case HIGH_DAMAGE:
			shape = Shape.CIRCLE;
			break;
		case DAMAGE:
			shape = Shape.TRIANGLE;
			break;
		case BUFF_ON:
			Buff buff = Buff.values()[ message.param  ];
			switch(buff) {
			case WEAKNESS:
				shape = Shape.Z;
				break;
			case CONCENTRATION:
				shape = Shape.V;
				break;
			case BLESSING:
				shape = Shape.PI;
				break;
			case HOLY_SHIELD:
				shape = Shape.SHIELD;
				break;
			default:
				shape = Shape.NONE;
			}
			break;
		case NEW_HP:
		case HEAL:
			shape = Shape.CLOCK;
			break;
		default:
			shape = Shape.NONE;
		}
		return shape;
	}
	
	public static FightAction getActionFromShape(Shape shape) {
		FightAction action;
		switch(shape) {
		case CIRCLE:
			action = FightAction.HIGH_DAMAGE;
			break;
		case TRIANGLE:
			action = FightAction.DAMAGE;
			break;
		case Z:
		case V:
		case PI:
		case SHIELD:
			action = FightAction.BUFF_ON;
			break;
		case CLOCK:
			action = FightAction.HEAL;
			break;
		default:
			action = FightAction.NONE;
		}
		return action;
	}

	public void setParam(int parameter) {
		param = parameter;
	}

	public static FightMessage fromBytes(byte[] bytes) {
		if(bytes.length < 3) { 
			Log.e("Wizard fight", "BYTE ARR LENGTH " + bytes.length);
			return null;
		}
		int actionIndex = (int)bytes[0];
		int parameter = ((int)bytes[1] << 8) | ((int)bytes[2] & 0xFF);
		return new FightMessage(actionIndex, parameter);
	}
	
	public byte[] getBytes() {
		byte[] b = new byte[3];
		b[0] = (byte) action.ordinal();
		b[1] = (byte) ((param >> 8) & 0xFF); //high byte
		b[2] = (byte) (param & 0xFF);        //low byte
		return b;
	}
	
	@Override 
	public String toString() {
		return action + " " + param;
	}
}
