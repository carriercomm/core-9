/*
 * Copyright (c) 2014 Vladislav Zablotsky
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.jprocessing.dao;

import com.jprocessing.entities.JpEntity;
import java.io.Serializable;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

/**
 *
 * @author rumatoest
 */
public interface JpaDao<PK extends Serializable, E extends JpEntity> {

    /**
     * Persist entity (create) to database
     */
    void persist(E entity) throws EntityExistsException;

    /**
     * Persist entity to database or merge it if entity already exist.
     */
    void persistOrMerge(E entity);

    /**
     * Merge the state of the given entity into the current persistence context.
     */
    void merge(E entity);

    /**
     * Refresh the state of the instance from the database, overwriting changes
     * made to the entity, if any.
     */
    void refresh(E entity) throws EntityNotFoundException;

    /**
     * Remove the entity instance from database.
     */
    void remove(E entity);

    /**
     * Remove the entity instance from database by primary key.
     */
    void remove(PK pk);

    /**
     * Return single entity by primary key or null if nothing was found.
     * Means that primary key field is unique.
     */
    E getByPk(PK pk);

    /**
     * Force to fetch all related data with FetchType.LAZY type.
     */
    E fetchRelated(E entity);

}
