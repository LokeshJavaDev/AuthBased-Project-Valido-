package io.service.valido.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.service.valido.commons.util.ServiceError;
import io.service.valido.model.HasIdentity;
import io.vavr.control.Either;


public interface ObjectRepository <ID extends Serializable, T extends HasIdentity<ID>>{
    /**
     * To create a Object
     * Return object
    **/

    Either<ServiceError, T> create(T object);

    /**
     * Fetch all objects.
     * @return list of all objects
     */
    Either<ServiceError, List<T>> fetch();

    /**
     * Retrieve an object by its ID.
     * @param id the object ID
     * @return Optional containing the object if found
     */
    Either<ServiceError, Optional<T>> retrieve(ID id);

    Either<ServiceError, List<T>> retrieve(Set<ID> id);

    /**
     * Update an object.
     * @param object the object to update
     * @return the updated object
     */
    Either<ServiceError, T> update(T object);

    /**
     * Delete an object by its ID.
     * @param id the object ID
     * @return true if deleted successfully, false otherwise
     */
    Either<ServiceError, Boolean> delete(ID id);

}