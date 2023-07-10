package com.brunosong.data_jpa.repository;

import com.brunosong.data_jpa.entity.Member;

import java.util.List;

public interface MemberRepositoryCustom {

    List<Member> findMemberCustom();

}
