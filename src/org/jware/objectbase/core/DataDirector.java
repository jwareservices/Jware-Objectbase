package org.jware.objectbase.core;

import org.jware.objectbase.exception.DataDirectorException;

/*
 * Copyright (C) 2015 J. Paul Jackson <jwareservices@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * File: DataDirector.java Created On: 00/23/2015
 *
 * @author J. Paul Jackson <jwareservices@gmail.com>
 *
 * Purpose:
 * @param <K> The key
 */
public interface DataDirector<K> {
    public void insertData(K key, Object data) throws DataDirectorException;
    public void updateData(K key, Object data) throws DataDirectorException;
    public void deleteData(K key) throws DataDirectorException;
    public Object retrieveData(K key) throws DataDirectorException;
}
