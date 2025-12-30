package io.service.valido.core;

import io.service.valido.commons.util.ServiceError;
import io.vavr.control.Either;

import java.io.Serializable;
import java.util.List;

public interface CRUDService<T, ID extends Serializable> {
    default Either<ServiceError, T> create(T entity) {
        return Either.left(new ServiceError(500, "Not implemented"));
    }

    default Either<ServiceError, T> retrieve(ID id) {
        return Either.left(new ServiceError(500, "Not implemented"));
    }

    default Either<ServiceError, T> update(T entity) {
        return Either.left(new ServiceError(500, "Not implemented"));
    }

    default Either<ServiceError, Boolean> delete(ID id) {
        return Either.left(new ServiceError(500, "Not implemented"));
    }
    default Either<ServiceError, List<T>> list() {
        return Either.left(new ServiceError(500, "Not implemented"));
    }
}
