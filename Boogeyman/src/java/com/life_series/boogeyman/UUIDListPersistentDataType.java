package com.life_series.boogeyman;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

class UUIDListPersistentDataType implements ListPersistentDataType<String, UUID> {

	@Override
	public Class<List<String>> getPrimitiveType() {
		return (Class) List.class;
	}

	@Override
	public Class<List<UUID>> getComplexType() {
		return (Class) List.class;
	}

	@Override
	public List<String> toPrimitive(List<UUID> complex, PersistentDataAdapterContext context) {
		List<String> primitive = new ArrayList<>();
		complex.forEach(uuid -> primitive.add(uuid.toString()));
		return primitive;
	}

	@Override
	public List<UUID> fromPrimitive(List<String> primitive, PersistentDataAdapterContext context) {
		List<UUID> complex = new ArrayList<>();
		primitive.forEach(string -> complex.add(UUID.fromString(string)));
		return complex;
	}

	@Override
	public PersistentDataType<String, UUID> elementType() {
		return new UUIDPersistentDataType();
	}

}
