package it.samvise85.bookshelf.persist.inmemory;

import it.samvise85.bookshelf.model.GenericIdentifiable;
import it.samvise85.bookshelf.model.Projectable;
import it.samvise85.bookshelf.persist.PersistOptions;
import it.samvise85.bookshelf.persist.PersistenceUnit;
import it.samvise85.bookshelf.persist.clauses.Order;
import it.samvise85.bookshelf.persist.clauses.OrderClause;
import it.samvise85.bookshelf.persist.clauses.PaginationClause;
import it.samvise85.bookshelf.persist.clauses.ProjectionClause;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public abstract class InMemoryPersistenceUnit<T extends GenericIdentifiable<?>> implements PersistenceUnit<T> {
	protected Class<T> registeredClass;
	protected static final List<OrderClause> DEFAULT_ORDER = Collections.singletonList(new OrderClause("id", Order.ASC));
	protected static final Logger log = Logger.getLogger(InMemoryPersistenceUnit.class);
	
	public InMemoryPersistenceUnit(Class<T> clazz) {
		registerClass(clazz);
	}

	private void registerClass(Class<T> clazz) {
		this.registeredClass = clazz;
	}

	public T get(Serializable id) {
		return getRepository().findOne(id);
	}

	@Override
	public T get(Serializable id, ProjectionClause projection) {
		T entity = getRepository().findOne(id);
		if(entity instanceof Projectable)
			if(entity != null) ((Projectable) entity).setProjection(projection);
		return entity;
	}

	@Override
	public List<T> getList(PersistOptions options) {
		Pageable pageable = createPageable(options != null ? options.getPagination() : null);
		Iterable<T> all = null;
		if(pageable == null)
			all = getRepository().findAll(); //TODO add selection and order
		else
			all = getRepository().findAll(pageable);
		
		return convertToList(all, options != null ? options.getProjection() : null);
	}

	public T create(T objectToSave) {
		return getRepository().save(objectToSave);
	}

	public T update(T objectToUpdate) {
		return getRepository().save(objectToUpdate);
	}

	public T delete(Serializable id) {
		getRepository().delete(id);
		return null;
	}

	public abstract <S extends Serializable> PagingAndSortingRepository<T, S> getRepository();
	
	protected List<T> convertToList(Iterable<T> iterable, ProjectionClause projectionClause) {
		List<T> res = new ArrayList<T>();
		Iterator<T> iterator = iterable.iterator();
		T previous = null;
		while(iterator.hasNext()) {
			T curr = iterator.next();
			if(previous == null || previous != curr) {
				if(curr instanceof Projectable)
					((Projectable)curr).setProjection(projectionClause);
				res.add((T) curr);
				previous = curr;
			} else break;
		}
		return res;
	}
	protected List<T> convertToList(Page<T> page, ProjectionClause projectionClause) {
		List<T> res = new ArrayList<T>();
		for(T curr : page) {
			if(curr instanceof Projectable)
				((Projectable)curr).setProjection(projectionClause);
			res.add((T) curr);
		}
		return res;
	}
	
	protected Pageable createPageable(PaginationClause pagination) {
		if(pagination == null) return null;
		return new PageRequest(pagination.getPage()-1, pagination.getPageSize());
	}

}