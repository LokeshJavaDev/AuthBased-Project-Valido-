package io.service.valido.model;

import java.io.Serializable;

public interface HasIdentity <ID extends Serializable>{
    ID getId();
}
