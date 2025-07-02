package com.showsync.repository;

import com.showsync.entity.Group;
import com.showsync.entity.GroupMediaVote;
import com.showsync.entity.Media;
import com.showsync.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for GroupMediaVote entity operations.
 * Manages voting on media within groups.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Repository
public interface GroupMediaVoteRepository extends JpaRepository<GroupMediaVote, Long> {

    /**
     * Find vote by group, media, and user
     * @param group the group
     * @param media the media
     * @param user the user
     * @return optional vote if found
     */
    Optional<GroupMediaVote> findByGroupAndMediaAndUser(Group group, Media media, User user);

    /**
     * Find all votes for a media item in a group
     * @param group the group
     * @param media the media
     * @return list of votes for the media
     */
    List<GroupMediaVote> findByGroupAndMedia(Group group, Media media);

    /**
     * Find all votes by a user in a group
     * @param group the group
     * @param user the user
     * @param pageable pagination information
     * @return page of votes by the user
     */
    Page<GroupMediaVote> findByGroupAndUserOrderByCreatedAtDesc(Group group, User user, Pageable pageable);

    /**
     * Find votes by type in a group
     * @param group the group
     * @param voteType the vote type
     * @param pageable pagination information
     * @return page of votes of the specified type
     */
    Page<GroupMediaVote> findByGroupAndVoteTypeOrderByCreatedAtDesc(
            Group group, GroupMediaVote.VoteType voteType, Pageable pageable);

    /**
     * Check if a user has voted on specific media in a group
     * @param group the group
     * @param media the media
     * @param user the user
     * @return true if user has voted
     */
    boolean existsByGroupAndMediaAndUser(Group group, Media media, User user);

    /**
     * Count votes by type for a media item in a group
     * @param group the group
     * @param media the media
     * @param voteType the vote type
     * @return count of votes
     */
    long countByGroupAndMediaAndVoteType(Group group, Media media, GroupMediaVote.VoteType voteType);

    /**
     * Get vote summary for a media item in a group
     * @param group the group
     * @param media the media
     * @return array with [watch_next_votes, skip_votes, priority_high_votes, priority_low_votes]
     */
    @Query("SELECT " +
           "SUM(CASE WHEN v.voteType = 'WATCH_NEXT' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN v.voteType = 'SKIP' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN v.voteType = 'PRIORITY_HIGH' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN v.voteType = 'PRIORITY_LOW' THEN 1 ELSE 0 END) " +
           "FROM GroupMediaVote v WHERE v.group = :group AND v.media = :media")
    Object[] getVoteSummary(@Param("group") Group group, @Param("media") Media media);

    /**
     * Calculate vote score for a media item in a group
     * @param group the group
     * @param media the media
     * @return calculated vote score
     */
    @Query("SELECT " +
           "SUM(CASE " +
           "    WHEN v.voteType = 'WATCH_NEXT' THEN 3 " +
           "    WHEN v.voteType = 'PRIORITY_HIGH' THEN 2 " +
           "    WHEN v.voteType = 'PRIORITY_LOW' THEN -1 " +
           "    WHEN v.voteType = 'SKIP' THEN -2 " +
           "    ELSE 0 " +
           "END) " +
           "FROM GroupMediaVote v WHERE v.group = :group AND v.media = :media")
    Long calculateVoteScore(@Param("group") Group group, @Param("media") Media media);

    /**
     * Find media with highest vote scores in a group
     * @param group the group
     * @return list of media IDs ordered by vote score descending
     */
    @Query("SELECT v.media.id, " +
           "SUM(CASE " +
           "    WHEN v.voteType = 'WATCH_NEXT' THEN 3 " +
           "    WHEN v.voteType = 'PRIORITY_HIGH' THEN 2 " +
           "    WHEN v.voteType = 'PRIORITY_LOW' THEN -1 " +
           "    WHEN v.voteType = 'SKIP' THEN -2 " +
           "    ELSE 0 " +
           "END) as score " +
           "FROM GroupMediaVote v WHERE v.group = :group " +
           "GROUP BY v.media.id ORDER BY score DESC")
    List<Object[]> findTopVotedMedia(@Param("group") Group group);

    /**
     * Find recent voting activity in a group
     * @param group the group
     * @param since only votes after this date
     * @param pageable pagination information
     * @return page of recent votes
     */
    @Query("SELECT v FROM GroupMediaVote v WHERE v.group = :group " +
           "AND v.createdAt >= :since ORDER BY v.createdAt DESC")
    Page<GroupMediaVote> findRecentVotes(@Param("group") Group group, 
                                        @Param("since") LocalDateTime since, 
                                        Pageable pageable);

    /**
     * Get voting statistics for a group
     * @param group the group
     * @return array with [total_votes, unique_media_voted_on, active_voters]
     */
    @Query("SELECT COUNT(v), COUNT(DISTINCT v.media), COUNT(DISTINCT v.user) " +
           "FROM GroupMediaVote v WHERE v.group = :group")
    Object[] getGroupVotingStatistics(@Param("group") Group group);

    /**
     * Delete all votes for a group (used when deleting a group)
     * @param group the group
     */
    void deleteByGroup(Group group);

    /**
     * Delete all votes for a media item
     * @param media the media
     */
    void deleteByMedia(Media media);

    /**
     * Delete all votes by a user
     * @param user the user
     */
    void deleteByUser(User user);
} 