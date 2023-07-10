package com.brunosong.data_jpa.controller;

import com.brunosong.data_jpa.dto.MemberDto;
import com.brunosong.data_jpa.entity.Member;
import com.brunosong.data_jpa.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        return memberRepository.findById(id).get().getUsername();
    }

    /* 이게 되긴 하지만 권장하지는 않는다. 이렇게 단순한 경우도 거의 없고 .. .내가 생각할때는 뭔가 명확함이 떨어진다.
       딱 조회용으로만 써야 한다. 트랜잭션 처리가 이상해서 그렇다. */
    @GetMapping("/members2/{id}")
    public String findMember(@PathVariable("id") Member member) {
        return member.getUsername();
    }

    /*  http://localhost:8080/members?page=0&size=3%sort=username,desc  이렇게 호출해도 처리가 가능
    *   글로벌 디폴트 값을 설정하려면
    *   spring.data.web.pageable.default-page-size=20 /# 기본 페이지 사이즈/
    *   spring.data.web.pageable.max-page-size=2000 /# 최대 페이지 사이즈/
    *   이렇게 수정하면 된다.
    *
    * */
    @GetMapping("/members")
    public Page<MemberDto> list(@PageableDefault(size = 12, sort = "username", direction = Sort.Direction.DESC) // 이렇게 처리도 가능하다.
            Pageable pageable) {

        Page<Member> page = memberRepository.findAll(pageable);
        //이상황은 Member 가 나가게 되기때문에 내부 문서를 거의다 공유한 형태이다.
        // 그래서 절대 Member 가 나가면 안된다. DTO로 변환해서 나가야 한다.

        Page<MemberDto> members = page.map(MemberDto::new);
        return members;
    }


    @PostConstruct
    public void init() {
        for (int i = 0; i < 100; i++) {
            memberRepository.save(new Member("user" + i, i));
        }
    }


}
