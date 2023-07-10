package com.brunosong.data_jpa.entity;

import com.brunosong.data_jpa.repository.MemberRepository;
import com.brunosong.data_jpa.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;


@SpringBootTest
@Transactional
@Rollback(value = false)
public class MemberTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    EntityManager em;

    @Test
    void 멤버엔티티_test() {

        //given
        Member member1 = new Member("brunosong1");
        Member member2 = new Member("brunosong2");
        Member member3 = new Member("brunosong3");

        Team team1 = new Team("alpha");
        Team team2 = new Team("beta");

        //when
        teamRepository.save(team1);
        teamRepository.save(team2);

        member1.setTeam(team1);
        member2.setTeam(team2);
        member3.setTeam(team2);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        em.flush();   // 바로 영속성컨텍스트에 모여있는것을 인썰트 쿼리를 날린다.
        em.clear();   // 영속성에 있는 캐쉬를 날린다. 초기화

        //then
        Team findTeam = teamRepository.findById(team2.getId()).get();

        System.out.println("================================");
        System.out.println("findTeam : " + findTeam.getName());
        System.out.println("findTeam : " + findTeam.getMembers());
        //팀에다가 멤버스를 넣어줘야 한다. 그렇지 않고 이렇게 검색을 하면 영속성컨텍스트에서 뽑아오는거라 가져오지 못한다.
        //그래서 영속성 말고 쿼리를 날릴라면 플러쉬를 해주면 가져올수 있을것 같다.
        //역시 그렇게 하니깐 된다.

        System.out.println("================================");

        for (Member m : findTeam.getMembers()) {
            System.out.println("팀 소속 멤버는  : " + m.getUsername());
        }



        //assertThat()
    }


    @Test
    public void JpaEvenBaseEntity() throws InterruptedException {

        //given
        Member member = new Member("member1");
        memberRepository.save(member); //@PrePersist 발생

        Thread.sleep(100);

        member.setUsername("member2");

        em.flush();    //@PreUpdate
        em.clear();


        //when
        Member findMember = memberRepository.findById(member.getId()).get();

        System.out.println("findMember.createdDate = " + findMember.getCreateDate());
        System.out.println("findMember.updatedDate = " + findMember.getLastModifiedDate());

    }


}
