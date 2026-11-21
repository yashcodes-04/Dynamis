/**
 * Date: 2026-11-18 - Yash
 * Syllabus: Unit 1 (Lecture 1) - Interfaces & Abstraction
 *           Unit 5 (Lecture 24) - Generic Interfaces & Type Safety
 */
package com.gym.dao;

import java.sql.SQLException;
import java.util.List;

public interface GenericDAO<T, ID> {
    List<T> getAll() throws SQLException;
    T findById(ID id) throws SQLException;
}
