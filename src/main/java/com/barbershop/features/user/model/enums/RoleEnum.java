package com.barbershop.features.user.model.enums;

public enum RoleEnum {
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_BARBER("ROLE_BARBER"),
    ROLE_CLIENT("ROLE_CLIENT");

    private final String roleName;

    RoleEnum(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
