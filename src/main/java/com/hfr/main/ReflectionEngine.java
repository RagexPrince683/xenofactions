package com.hfr.main;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.ReflectionHelper;

public class ReflectionEngine {

	/**
	 * Will search object "instance" for fields of the type "type" and return a list.
	 * @param type
	 * @param clazz
	 * @return
	 */
	public static List<Field> crackOpenAColdOne(Class<?> type, Class<?> clazz) {
		List<Field> fields = new ArrayList<Field>();

		for (Field field : clazz.getFields()) {
			if (field.getType().isAssignableFrom(type)) {
				fields.add(field);
			}
		}

		if (clazz.getSuperclass() != null) {
			fields.addAll(crackOpenAColdOne(type, clazz.getSuperclass()));
		}

		return fields;
	}

	public static void setDoubleToZero(Object o, String name) {
		Class<?> clazz = o.getClass();

		while (clazz != null) {
			try {
				Field field = ReflectionHelper.findField(clazz, name);
				field.setAccessible(true);
				Object val = field.get(o);

				if (val instanceof Double) {
					field.setDouble(o, 0);
				}
			} catch (Exception x) {
				System.err.println("Error setting double to zero: " + x.getMessage());
				x.printStackTrace();
			}
			clazz = clazz.getSuperclass();
		}
	}

	public static List<Object> pryObjectsFromFieldList(List<Field> fields, Object o) {
		List<Object> objects = new ArrayList<Object>();

		for (Field field : fields) {
			try {
				field.setAccessible(true);
				objects.add(field.get(o));
			} catch (Exception ex) {
				System.err.println("Error prying object from field: " + ex.getMessage());
				ex.printStackTrace();
			}
		}

		return objects;
	}

	public static <T> T hasValue(Object e, Class<T> ret, String name, T def) {
		if (e == null) return def;

		Class<?> clazz = e.getClass();

		while (clazz != null) {
			try {
				Field field = ReflectionHelper.findField(clazz, name);
				field.setAccessible(true);
				Object val = field.get(e);

				if (val != null) {
					return ret.cast(val);
				}
			} catch (Exception x) {
				System.err.println("Error accessing value: " + x.getMessage());
				x.printStackTrace();
			}
			clazz = clazz.getSuperclass();
		}

		return def;
	}

	public static Object getVehicleFromSeat(Object e) {
		if (e == null || !MainRegistry.enableRadar) return null;

		Object driveable = hasValue(e, Object.class, "driveable", null);

		if (driveable == null) {
			return null;
		}

		return driveable;
	}
}