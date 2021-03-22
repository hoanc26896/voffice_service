--- DB 2 Voffice Extract----
CREATE TABLE GROUP_SIGN 
( GROUP_SIGN_ID NUMBER NOT NULL,
  GROUP_ID  NUMBER NOT NULL,
  ORG_VHR_ID NUMBER(22,0),
  ORG_VHR_NAME NVARCHAR2(2000),
  ROLE_ID NUMBER(30,0),
  EMP_VHR_ID NUMBER(22,0),
  SIGN_LEVEL_PARALLEL NUMBER(3,0),
  SIGN_IMAGE_ID NUMBER(22,0),
  SIGN_LEVEL NUMBER(10,0),
  SIGN_IMAGE NUMBER(3,0),
  IS_PUBLISH NUMBER(1,0),
  CREATED_DATE DATE,
  CREATED_BY NUMBER(22,0),
  UPDATED_DATE DATE,
  UPDATED_BY NUMBER(22,0),
  DELETED_DATE DATE,
  DELETED_BY NUMBER(22,0),
  DEL_FLAG NUMBER(1,0),
  "ORDER" NUMBER(10,0)
 );


CREATE SEQUENCE GROUP_SIGN_SEQ START WITH 1;

commit;