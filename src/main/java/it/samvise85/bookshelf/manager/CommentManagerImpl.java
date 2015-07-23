package it.samvise85.bookshelf.manager;

import it.samvise85.bookshelf.model.Comment;
import it.samvise85.bookshelf.model.Stream;
import it.samvise85.bookshelf.persist.AbstractPersistenceUnit;
import it.samvise85.bookshelf.persist.clauses.ProjectionClause;
import it.samvise85.bookshelf.persist.repository.CommentRepository;

import java.util.Date;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentManagerImpl extends AbstractPersistenceUnit<Comment> implements CommentManager {
	@Autowired
	private CommentRepository repository;
	@Autowired
	protected StreamManager streamManager;
	
	public CommentManagerImpl() {
		super(Comment.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CommentRepository getRepository() {
		return repository;
	}

	@Override
	public Comment create(Comment objectToSave) {
		Date date = new Date();
		if(objectToSave.getCreation() == null) {
			objectToSave.setCreation(date);
		} else {
			date = objectToSave.getCreation();
		}
		objectToSave.setLastModification(date);
		objectToSave.setId(objectToSave.getUser()+date.getTime());
		return addStream(super.create(objectToSave));
	}

	@Override
	public Comment update(Comment updates) {
		Comment commentToUpdate = get(updates.getId(), ProjectionClause.NO_PROJECTION);
		Date date = new Date();
		if(commentToUpdate.getCreation() == null)
			commentToUpdate.setCreation(date);
		commentToUpdate.setLastModification(date);
		commentToUpdate.setComment(updates.getComment());
		return super.update(commentToUpdate);
	}

	@Transactional(value=TxType.REQUIRES_NEW)
	private Comment addStream(Comment comm) {
		if(comm.getStream() == null) {
			Stream stream = streamManager.create();
			comm.setStream(stream.getId());
			super.update(comm);
		}
		return comm;
	}
}
