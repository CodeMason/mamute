package br.com.caelum.brutal.migration.all;

import static java.util.Arrays.asList;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.StatelessSession;

import br.com.caelum.brutal.migration.Migration;
import br.com.caelum.brutal.migration.MigrationOperation;
import br.com.caelum.brutal.migration.RawSQLOperation;
import br.com.caelum.brutal.model.Answer;
import br.com.caelum.brutal.model.AnswerInformation;
import br.com.caelum.brutal.model.EventType;
import br.com.caelum.brutal.model.Question;
import br.com.caelum.brutal.model.QuestionInformation;
import br.com.caelum.brutal.model.ReputationEvent;
import br.com.caelum.brutal.model.User;
import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;

@Component
@ApplicationScoped
public class M017InsertAcceptededEditsReputationEvents implements Migration {

	@Override
	public List<MigrationOperation> up() {
		MigrationOperation operation = new MigrationOperation() {
			@Override
			@SuppressWarnings("unchecked")
			public void execute(Session session, StatelessSession statelessSession) {
				List<QuestionInformation> informations = session.createCriteria(QuestionInformation.class).list();
				for (QuestionInformation information : informations) {
					Question question = information.getQuestion();
					User questionAuthor = question.getAuthor();
					User editAuthor = information.getAuthor();
					if (!questionAuthor.getId().equals(editAuthor.getId())) {
						statelessSession.insert(new ReputationEvent(EventType.EDIT_APPROVED, question, editAuthor));
					}
				}
				List<AnswerInformation> answerInformations = session.createCriteria(AnswerInformation.class).list();
				for (AnswerInformation answerInformation : answerInformations) {
					Answer answer = answerInformation.getAnswer();
					User answerAuthor = answer.getAuthor();
					User editAuthor = answerInformation.getAuthor();
					if (!answerAuthor.getId().equals(editAuthor.getId())) {
						statelessSession.insert(new ReputationEvent(EventType.EDIT_APPROVED, answer.getQuestion(), editAuthor));
					}
				}
				
			}
		};
		return asList(operation);
	}

	@Override
	public List<MigrationOperation> down() {
		return RawSQLOperation.forSqls("");
	}

}
