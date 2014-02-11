package org.thriftee.examples.presidents;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
	public int getPresidentsCountWithFilter(PresidentFilter filter) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<President> getPresidentsWithFilterAndSort(
			PresidentFilter filter, PresidentSort sort, int rowStart, int rowEnd) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, President> getPresidentsByUniqueIds(String property,
			List<String> uniqueIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(President president) {
		// TODO Auto-generated method stub
		
	}

	
	
}
