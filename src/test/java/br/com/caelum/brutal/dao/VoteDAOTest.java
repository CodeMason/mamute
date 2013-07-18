package br.com.caelum.brutal.dao;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.brutal.dto.SuspectMassiveVote;
import br.com.caelum.brutal.model.Answer;
import br.com.caelum.brutal.model.Comment;
import br.com.caelum.brutal.model.CommentsAndVotes;
import br.com.caelum.brutal.model.LoggedUser;
import br.com.caelum.brutal.model.MassiveVote;
import br.com.caelum.brutal.model.Question;
import br.com.caelum.brutal.model.Tag;
import br.com.caelum.brutal.model.User;
import br.com.caelum.brutal.model.Vote;
import br.com.caelum.brutal.model.VoteType;
import br.com.caelum.brutal.model.VotingMachine;
import br.com.caelum.brutal.model.interfaces.Votable;
import br.com.caelum.brutal.reputation.rules.KarmaCalculator;

public class VoteDAOTest extends DatabaseTestCase{
	
	private VoteDAO votes;
	private User currentUser;
	private User otherUser;
	private VotingMachine votingMachine;
	private List<Tag> tags = new ArrayList<>();
	
	@Before
	public void beforeTest() {
		votes = new VoteDAO(session);
		currentUser = user("Current User", "currentUser@caelum.com");
		otherUser = user("Other User", "otherUser@caelum.com");
		session.save(otherUser);
		session.save(currentUser);
		tags.add(tag("bla"));
		for (Tag tag : tags) {
			session.save(tag);
		}
		InvisibleForUsersRule invisibleRule = new InvisibleForUsersRule(new LoggedUser(currentUser, null));
		votingMachine = new VotingMachine(votes, new KarmaCalculator(), new ReputationEventDAO(session, invisibleRule), new MassiveVote());
	}
	
	@Test
	public void should_return_right_comments_and_currentUser_votes_map() {
		
		Question question = question(currentUser, tags);
		
		Answer answer = answer("blablablablablablablablablablbalblabla", question, currentUser);
		
		Comment answerComment1 = comment(otherUser, "comentariocomentariocomentariocomentariocomentario");
		Comment answerComment2 = comment(currentUser, "comentariocomentariocomentariocomentariocomentario");
		Comment answerComment3 = comment(otherUser, "comentariocomentariocomentariocomentariocomentario");
		
		answer.add(answerComment1);
		answer.add(answerComment2);
		answer.add(answerComment3);
		
		Comment questionComment1 = comment(otherUser, "comentariocomentariocomentariocomentariocomentario");
		Comment questionComment2 = comment(currentUser, "comentariocomentariocomentariocomentariocomentario");
		Comment questionComment3 = comment(otherUser, "comentariocomentariocomentariocomentariocomentario");
		
		question.add(questionComment1);
		question.add(questionComment2);
		question.add(questionComment3);
		
		Vote currentUserUpVote1 = upvote(answerComment1, currentUser);
		Vote currentUserUpVote2 = upvote(questionComment1, currentUser);
		
		upvote(answerComment2, otherUser);
		upvote(questionComment2, otherUser);
		
		session.save(question);
		session.save(answer);
		session.save(answerComment1);
		session.save(answerComment2);
		session.save(answerComment3);
		
		CommentsAndVotes commentsAndVotes = votes.previousVotesForComments(question, currentUser);
		
		assertEquals(currentUserUpVote1, commentsAndVotes.getVotes(answerComment1));
		assertEquals(currentUserUpVote2, commentsAndVotes.getVotes(questionComment1));
		
		assertEquals(null, commentsAndVotes.getVotes(questionComment2));
		assertEquals(null, commentsAndVotes.getVotes(answerComment2));
		
		assertEquals(null, commentsAndVotes.getVotes(questionComment3));
		assertEquals(null, commentsAndVotes.getVotes(answerComment3));
	}
	
	@Test
	public void should_find_question_from_votable() throws Exception {
		Question question = question(currentUser, tags);
		Answer answer = answer("answer answer answer answer answer", question, currentUser);
		Comment comment = comment(currentUser, "blabla blabla blabla blabla blabla blabla");
		question.add(comment);
		
		session.save(question);
		session.save(answer);
		session.save(comment);
		
		assertEquals(question, votes.questionOf(question));
		assertEquals(question, votes.questionOf(answer));
		assertEquals(question, votes.questionOf(comment));
		
	}
	
	@Test
	public void testName() throws Exception {
		Question question = question(currentUser, tags);
		Answer answer = answer("answer answer answer answer answer", question, currentUser);
		
		Question question2 = question(currentUser, tags);
		Answer answer2 = answer("answer answer answer answer answer", question2, currentUser);
		
		Question question3 = question(currentUser, tags);
		Answer answer3 = answer("answer answer answer answer answer", question3, currentUser);
		
		session.save(question);
		session.save(question2);
		session.save(question3);
		
		session.save(answer);
		session.save(answer2);
		session.save(answer3);

		upvote(answer, otherUser);
		upvote(answer2, otherUser);
		upvote(answer3, otherUser);
		
		List<SuspectMassiveVote> answers = votes.suspectMassiveVote(VoteType.UP, new DateTime().minusHours(1), new DateTime());
		
		assertEquals(otherUser,answers.get(0).getVoteAuthor());
		assertEquals(currentUser,answers.get(0).getAnswerAuthor());
		assertEquals(3l,answers.get(0).getMassiveVoteCount().longValue());
		
	}
	

	private Vote upvote(Votable votable, User user) {
		Vote vote = new Vote(user, VoteType.UP);
		session.save(votable);
		session.save(vote);
		votingMachine.register(votable, vote, Comment.class);
		return vote;
	}
}
