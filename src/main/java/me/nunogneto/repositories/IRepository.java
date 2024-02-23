package me.nunogneto.repositories;

import java.util.Optional;

public interface IRepository<T, ID> {

    Optional<T> findById(ID id);

    T save(T entity);

}
