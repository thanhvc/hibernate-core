/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010 by Red Hat Inc and/or its affiliates or by
 * third-party contributors as indicated by either @author tags or express
 * copyright attribution statements applied by the authors.  All
 * third-party contributions are distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.metamodel.relational;

import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.source.MetadataImplementor;

/**
 * Models a SQL <tt>INDEX</tt>
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public class Index extends AbstractConstraint implements Constraint {
	protected Index(Table table, String name) {
		super( table, name );
	}

	public String[] sqlCreateStrings(MetadataImplementor metadata) {
		return new String[] {
				buildSqlCreateIndexString(
						getDialect( metadata ),
					getName(),
					getTable(),
					getColumns(),
					false
				)
		};
	}

	/* package-protected */
	static String buildSqlDropIndexString(
			Dialect dialect,
			TableSpecification table,
			String name	) {
		return "drop index " +
				StringHelper.qualify(
						table.getQualifiedName( dialect ),
						name
				);
	}

	public static String buildSqlCreateIndexString(
			Dialect dialect,
			String name,
			TableSpecification table,
			Iterable<Column> columns,
			boolean unique
	) {
		//TODO handle supportsNotNullUnique=false, but such a case does not exist in the wild so far
		StringBuilder buf = new StringBuilder( "create" )
				.append( unique ?
						" unique" :
						"" )
				.append( " index " )
				.append( dialect.qualifyIndexName() ?
						name :
						StringHelper.unqualify( name ) )
				.append( " on " )
				.append( table.getQualifiedName( dialect ) )
				.append( " (" );
		boolean first = true;
		for ( Column column : columns ) {
			if ( first ) {
				first = false;
			}
			else {
				buf.append( ", " );
			}
			buf.append( ( column.getColumnName().encloseInQuotesIfQuoted( dialect ) ) );
		}
		buf.append( ")" );
		return buf.toString();
	}

	public String sqlConstraintStringInAlterTable(Dialect dialect) {
		StringBuilder buf = new StringBuilder( " index (" );
		boolean first = true;
		for ( Column column : getColumns() ) {
			if ( first ) {
				first = false;
			}
			else {
				buf.append( ", " );
			}
			buf.append( column.getColumnName().encloseInQuotesIfQuoted( dialect ) );
		}
		return buf.append( ')' ).toString();
	}

	public String[] sqlDropStrings(MetadataImplementor metadata) {
		return new String[] {
				new StringBuffer( "drop index " )
				.append(
						StringHelper.qualify(
								getTable().getQualifiedName( getDialect( metadata ) ),
								getName()
						)
				).toString()
		};
	}
}
