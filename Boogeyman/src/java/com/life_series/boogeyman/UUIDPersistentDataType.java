package com.life_series.boogeyman;

import java.util.UUID;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

class UUIDPersistentDataType implements PersistentDataType<String, UUID> {

	@Override
	public Class<String> getPrimitiveType() {
		return String.class;
	}

	@Override
	public Class<UUID> getComplexType() {
		return UUID.class;
	}

	@Override
	public String toPrimitive(UUID complex, PersistentDataAdapterContext context) {
		return complex.toString();
	}

	@Override
	public UUID fromPrimitive(String primitive, PersistentDataAdapterContext context) {
		return UUID.fromString(primitive);
	}

}
