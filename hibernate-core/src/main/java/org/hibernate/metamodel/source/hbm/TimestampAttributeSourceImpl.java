/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
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
package org.hibernate.metamodel.source.hbm;

import java.util.List;
import java.util.Map;

import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.internal.util.Value;
import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.binder.ExplicitHibernateTypeSource;
import org.hibernate.metamodel.source.binder.MetaAttributeSource;
import org.hibernate.metamodel.source.binder.RelationalValueSource;
import org.hibernate.metamodel.source.binder.SingularAttributeNature;
import org.hibernate.metamodel.source.binder.SingularAttributeSource;
import org.hibernate.metamodel.source.hbm.jaxb.mapping.XMLHibernateMapping;

/**
 * Implementation for {@code <timestamp/>} mappings
 *
 * @author Steve Ebersole
 */
class TimestampAttributeSourceImpl implements SingularAttributeSource {
	private final XMLHibernateMapping.XMLClass.XMLTimestamp timestampElement;
	private final LocalBindingContext bindingContext;
	private final List<RelationalValueSource> valueSources;

	TimestampAttributeSourceImpl(
			final XMLHibernateMapping.XMLClass.XMLTimestamp timestampElement,
			LocalBindingContext bindingContext) {
		this.timestampElement = timestampElement;
		this.bindingContext = bindingContext;
		this.valueSources = Helper.buildValueSources(
				new Helper.ValueSourcesAdapter() {
					@Override
					public String getColumnAttribute() {
						return timestampElement.getColumn();
					}

					@Override
					public String getFormulaAttribute() {
						return null;
					}

					@Override
					public List getColumnOrFormulaElements() {
						return null;
					}
				},
				bindingContext
		);
	}

	private final ExplicitHibernateTypeSource typeSource = new ExplicitHibernateTypeSource() {
		@Override
		public String getName() {
			return "db".equals( timestampElement.getSource() ) ? "dbtimestamp" : "timestamp";
		}

		@Override
		public Map<String, String> getParameters() {
			return null;
		}
	};

	@Override
	public String getName() {
		return timestampElement.getName();
	}

	@Override
	public ExplicitHibernateTypeSource getTypeInformation() {
		return typeSource;
	}

	@Override
	public String getPropertyAccessorName() {
		return timestampElement.getAccess();
	}

	@Override
	public boolean isInsertable() {
		return true;
	}

	@Override
	public boolean isUpdatable() {
		return true;
	}

	private Value<PropertyGeneration> propertyGenerationValue = new Value<PropertyGeneration>(
			new Value.DeferredInitializer<PropertyGeneration>() {
				@Override
				public PropertyGeneration initialize() {
					final PropertyGeneration propertyGeneration = timestampElement.getGenerated() == null
							? PropertyGeneration.NEVER
							: PropertyGeneration.parse( timestampElement.getGenerated().value() );
					if ( propertyGeneration == PropertyGeneration.INSERT ) {
						throw new MappingException(
								"'generated' attribute cannot be 'insert' for versioning property",
								bindingContext.getOrigin()
						);
					}
					return propertyGeneration;
				}
			}
	);

	@Override
	public PropertyGeneration getGeneration() {
		return propertyGenerationValue.getValue();
	}

	@Override
	public boolean isLazy() {
		return false;
	}

	@Override
	public boolean isIncludedInOptimisticLocking() {
		return false;
	}

	@Override
	public Iterable<CascadeStyle> getCascadeStyle() {
		return Helper.NO_CASCADING;
	}

	@Override
	public SingularAttributeNature getNature() {
		return SingularAttributeNature.BASIC;
	}

	@Override
	public boolean isVirtualAttribute() {
		return false;
	}

	@Override
	public List<RelationalValueSource> relationalValueSources() {
		return valueSources;
	}

	@Override
	public boolean isSingular() {
		return true;
	}

	@Override
	public Iterable<MetaAttributeSource> metaAttributes() {
		return Helper.buildMetaAttributeSources( timestampElement.getMeta() );
	}
}
