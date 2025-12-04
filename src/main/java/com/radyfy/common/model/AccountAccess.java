package com.radyfy.common.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountAccess extends BaseEntityModel {
  private String userId;
  private String[] include;
  private String[] exclude;
  // if all access then null or []
  // eg.  collectionname_enityrecordId, collectionname_enityrecordId, ...
}


//admin with a account access
//access: [account_aid1]
// this user login in radyfy
// 


//admin with a account access
//access: [account_aid2]
// this user login in eduempower: school1, ecom_account_school1, account_aid2
// 




//user with a one branch access, 
//access: [account_aid1, school_sid2, branch_bid2]
// this user login in school_branch, should have access: branch_bid2, school_sid2, account_aid1,