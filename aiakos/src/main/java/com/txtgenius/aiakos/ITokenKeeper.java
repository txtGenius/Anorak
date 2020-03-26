package com.txtgenius.aiakos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface ITokenKeeper<TOKEN> {

    @Nullable
    TOKEN getToken();

    void setToken(@NotNull TOKEN token);
}
