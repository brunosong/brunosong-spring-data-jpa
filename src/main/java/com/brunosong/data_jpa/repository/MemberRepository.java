package com.brunosong.data_jpa.repository;

import com.brunosong.data_jpa.dto.MemberDto;
import com.brunosong.data_jpa.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> , MemberRepositoryCustom {

    List<Member> findByUsername(String username); //이걸 쿼리 메소드라고 한다.

    /* 이름 다르게 하면 안된다. UsernameAndAgeGreaterThan 이게 조건절이라고 생각하면 된다. */
    /* 쿼리 메소드 필터 조건 스프링 데이터 JPA 공식 문서 참고: (https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation */
    /* 모든게 다 되는건 아니다. */
    List<Member> findByUsernameAndAgeGreaterThan(String username,int age);

    @Query("select m from Member m where m.username = :username and m.age = :age")    //오타 검색까지 해준다. 로딩시점에서 에러를 발생시킨다.
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsername();

    @Query("select new com.brunosong.data_jpa.dto.MemberDto(m.id ,m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);

    List<Member> findListByUsername(String username);   //컬랙션

    Member findMemberByUsername(String username);       //단건

    Optional<Member> findOptionalByUsername(String username);  // 옵셔널   반환타입을 유연하게 쓸수 있다.

    Page<Member> findByAge(int age, Pageable pageable);

    @Modifying //(clearAutomatically = true) 이게 있으면 자동으로 영속성컨텍스트를 클리어 한다. // @Modifying 이게 있어야 작동한다. 아니면 에러난다.
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);


    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();


    @Override
    @EntityGraph(attributePaths = {"team"})  //귀찮아 jpql로 하기 싫어 이럴때 쓰면 된다.
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();       // 이렇게 해도 가능하다.


    //@EntityGraph(attributePaths = {"team"})
    @EntityGraph("Member.all")
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    // 힌트를 사용해서 리드온리를 사용하게 되면 더티채킹이 되지 않는다.
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly" , value = "true"))
    Member findReadOnlyByUsername(String username);

    // select for update 비관적인락 ??? 내가 업데이트를 할때 다른 애들은 손대지마 뭐 그런 락이 있다고 한다. 그걸 jpa에서도 지원한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String name);


}
