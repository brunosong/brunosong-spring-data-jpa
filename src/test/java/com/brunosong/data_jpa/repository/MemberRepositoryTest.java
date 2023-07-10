package com.brunosong.data_jpa.repository;

import com.brunosong.data_jpa.dto.MemberDto;
import com.brunosong.data_jpa.entity.Member;
import com.brunosong.data_jpa.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    /* 스프링 DATA JPA 사용 */

    @Autowired
    MemberRepository repository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    EntityManager em;

    @Test
    void 멤버_DATAJPA_test() {

        //given
        Member member = new Member("Brunosong");

        //when
        Member saveMember = repository.save(member);
        Member findMember = repository.findById(member.getId()).get();

        //then
        assertThat(saveMember.getId()).isEqualTo(findMember.getId());
        assertThat(saveMember.getUsername()).isEqualTo(findMember.getUsername());
        //같은 트랜잭션 안에서는 영속성컨텍스트에서 보장이 된다.  1차 캐시라고 한다.
        assertThat(findMember).isEqualTo(saveMember);
    }


    @Test
    void findByUsernameAndAgeGreaterThan() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        repository.save(m1);
        repository.save(m2);

        List<Member> result = repository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);

    }


    @Test
    void testQuery() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        repository.save(m1);
        repository.save(m2);

        List<Member> result = repository.findUser("AAA", 10);

        assertThat(result.get(0)).isEqualTo(m1);

    }


    @Test
    void testStringQuery() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        repository.save(m1);
        repository.save(m2);

        List<String> result = repository.findUsername();
        for (String s : result) {
            System.out.println("유저 이름  : " + result);
        }
    }


    @Test
    void testMemberDto() {

        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");

        teamRepository.save(team1);
        teamRepository.save(team2);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team1);
        Member m2 = new Member("BBB", 20);
        m2.setTeam(team2);

        repository.save(m1);
        repository.save(m2);

        List<MemberDto> result = repository.findMemberDto();
        for (MemberDto dto : result) {
            System.out.println(dto);
        }
    }


    @Test
    void findByNames() {

        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");

        teamRepository.save(team1);
        teamRepository.save(team2);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team1);
        Member m2 = new Member("BBB", 20);
        m2.setTeam(team2);

        repository.save(m1);
        repository.save(m2);

        List<Member> result = repository.findByNames(Arrays.asList("AAA","BBB"));
        for (Member dto : result) {
            System.out.println(dto);
        }
    }


    @Test
    void returnTypeTest() {

        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        repository.save(m1);
        repository.save(m2);

        List<Member> aaa = repository.findListByUsername("AAA");
        System.out.println(aaa);

        Member bbb = repository.findMemberByUsername("BBB");
        System.out.println(bbb);

        Optional<Member> bbb1 = repository.findOptionalByUsername("BBBAA");
        Member member = bbb1.orElse(null);
        System.out.println(member);
    }


    @Test
    public void paging() {

        //given
        repository.save(new Member("member1", 10));
        repository.save(new Member("member2", 10));
        repository.save(new Member("member3", 10));
        repository.save(new Member("member4", 10));
        repository.save(new Member("member5", 10));

        int age = 10;

        PageRequest pageRequest = PageRequest.of(0, 3,
                Sort.by(Sort.Direction.DESC, "username"));

        //when
        Page<Member> page = repository.findByAge(age, pageRequest);
        //Slice<Member> page = repository.findByAge(age, pageRequest); Slice 도 가능하다. 이건 약간 다르다. getTotalElements , getTotalPages 이 메소드가 존재하지 않는다.
        // Slice 는 모바일에서 더보기 기능같은곳에서 쓸라고 나온 API 이다.
        // 실무 꿀팁 : Member 는 Api 로 나가면 안된다. 결국은 DTO로 변환해야 한다.
        // Page<MemberDto> pageMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null)); 이렇게 변환을 해줘야 한다.


        //then
        List<Member> content = page.getContent();
        long totalElements = page.getTotalElements();

        assertThat(content.size()).isEqualTo(3);
        assertThat(totalElements).isEqualTo(5);

        assertThat(page.getNumber()).isEqualTo(0); //현재페이지
        assertThat(page.getTotalPages()).isEqualTo(2);  //총 페이지
        assertThat(page.isFirst()).isTrue();
        assertThat(page.hasNext()).isTrue();

    }

    @Test
    public void bulkUpdate() throws Exception {

        //given
        repository.save(new Member("member1", 10));
        repository.save(new Member("member2", 19));
        repository.save(new Member("member3", 20));
        repository.save(new Member("member4", 21));
        repository.save(new Member("member5", 40));

        //when
        int resultCount = repository.bulkAgePlus(20);
        //벌크연산은 무조건 디비에 때리는거라서 영속성 컨텍스트에 반영이 안된다.

        //이렇게 조회를 하게 되면 40으로 나올꺼다.
        List<Member> member5 = repository.findByUsername("member5");
        System.out.println(member5);
        assertThat(member5.get(0).getAge()).isEqualTo(40);

        //그래서 벌크 연산에서 조심해야 하는 부분이다. 저 member5는 영속성 컨텍스트에서 꺼내오는거라 디비에 있는 자료가 아니다.
        //그래서 벌크 연산 이후에는 무조건 영속성 컨텍스트를 날려 버려야 한다.
        //JPQL 은 쿼리 날리기 전에 flush 를 해주고 한다. 이부분을 좀더 알아봐야 한다. 기본편을 봐야 한다.


        em.flush();
        em.clear();

        List<Member> member5_2 = repository.findByUsername("member5");
        System.out.println(member5_2);
        assertThat(member5_2.get(0).getAge()).isEqualTo(41);

        //then
        assertThat(resultCount).isEqualTo(3);

    }


    @Test
    public void findMemberLazy() {

        //member1 -> teamA
        //member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        repository.save(new Member("member1", 10, teamA));
        repository.save(new Member("member2", 20, teamB));

        em.flush();
        em.clear();

        //List<Member> members = repository.findAll();  //@EntityGraph 를 쓰면 패치 조인과 같은 기능을 얻을 수 있다.
        //List<Member> memberFetchJoin = repository.findMemberFetchJoin();   // fetch join 사용으로 인해 한번에 셀렉트를 해서 다 가지고 온다.
        List<Member> members1 = repository.findEntityGraphByUsername("member1");
        List<Member> members2 = repository.findEntityGraphByUsername("member2");

        for (Member member : members1) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member = " + member.getTeam().getName()); //여기서 쿼리가 한번 더 나감
        }

        for (Member member : members2) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member = " + member.getTeam().getName()); //여기서 쿼리가 한번 더 나감
        }


    }



    @Test
    public void queryHint() throws Exception {

        //given
        repository.save(new Member("member1", 10));

        em.flush();
        em.clear();

        //when
        // Member member2 = repository.findById(member1.getId()).get();
        // member2.setUsername("member2");   //이렇게 하면 일단은 메모리에 값을 자기고 온후에 영속성 컨텍스트에 값이 변경된다. 터티채킹으로 인해서 값이 바뀐다.
        // 결국 트랜잭션이 끝나면 업데이트 문이 날아간다.
        // 조회용으로만 쓰니깐 다 리드온리로 해야지 하고 해봐야 거의 차이가 안난다. 쿼리가 잘못나가서 장애가 나는거지 이렇게 까지 할꺼냐 라고 하면 진짜 중요하고
        // 트래픽이 너무 많은 경우만 넣고 왠만하면 안넣는게 편하다. 결국 성능테스트를 하고 중요하게 얻는 이점이 있다고 생각할때만 한다.
        // 하지만 거의 이전에 트래픽이 많으면 그 전에 레디스나 캐쉬를 이용해서 처리를 할꺼기 때문에


        Member member = repository.findReadOnlyByUsername("member1");
        member.setUsername("member2");

        em.flush(); //Update Query 실행X
    }


    @Test
    public void lock() throws Exception {

        //given
        repository.save(new Member("member1", 10));

        em.flush();
        em.clear();

        //when

        /*
        * from
            member member0_
        where
            member0_.username=? for update  마지막에 for update 가 붙는다.
        *
        * */
        List<Member> result = repository.findLockByUsername("member1");

    }


    @Test
    public void callCustom(){

        List<Member> result = repository.findMemberCustom();


    }






}