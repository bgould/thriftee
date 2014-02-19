package org.thriftee.examples.presidents;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

@Stateless
@Remote(PresidentService.class)
public class PresidentServiceBean implements PresidentService {

	@PersistenceContext
	private EntityManager em;
	
	@Override
	public List<President> getPresidents() {
		return em.createQuery("select p from President p", President.class).getResultList();
	}
	
	@Override
	public President getPresidentByUniqueId(int id) {
		return em.createQuery("select p from President p where p.id = ?", President.class).setParameter(1, id).getSingleResult();
	}

	@Override
	public int getPresidentsCountWithFilter(PresidentFilter filter) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		filter.execute(cb, query);
		query.select(cb.count(query.from(President.class)));
		return em.createQuery(query).getSingleResult().intValue();
	}

	@Override
	public List<President> getPresidentsWithFilterAndSort(
			PresidentFilter filter, PresidentSort sort, int rowStart, int rowEnd) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<President> query = cb.createQuery(President.class);
		filter.execute(cb, query);
		sort.execute(cb, query);
		return em.createQuery(query).getResultList();
	}

	@Override
	public Map<String, President> getPresidentsByUniqueIds(String property,	List<String> uniqueIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(President president) {
		em.persist(president);
	}

	
	
}
