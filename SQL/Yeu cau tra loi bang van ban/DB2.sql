--------------------------------------------------------
--  DDL for Type T_TABLE
--------------------------------------------------------
create or replace TYPE T_TABLE IS OBJECT
(
    ID int
    , VALUE VARCHAR2(2000)
);

--------------------------------------------------------
--  DDL for Type T_TABLE_COLL
--------------------------------------------------------
create or replace TYPE T_TABLE_COLL IS TABLE OF T_TABLE;

--------------------------------------------------------
--  DDL for FUNCTION F_MAP_MEETING_DOC_IDS
--------------------------------------------------------
CREATE OR REPLACE FUNCTION F_MAP_MEETING_DOC_IDS
  RETURN T_TABLE_COLL
IS
  l_res_coll T_TABLE_COLL;
  l_index NUMBER;
BEGIN
  l_res_coll := T_TABLE_COLL();
  FOR i IN
  ( WITH TAB AS
  (
  --SELECT '1' ID, 'a/B/C/D/E/F' STR FROM DUAL
  SELECT m.ID                                 AS id,
    SUBSTR(m.DOC_IDS,2,LENGTH(m.DOC_IDS) - 2) AS STR
  FROM MEETING m
  WHERE m.DEL_FLAG = 0
  AND m.DOC_IDS   IS NOT NULL
  )
SELECT id,
  SUBSTR(STR, instr(STR, '/', 1, lvl) + 1, instr(STR, '/', 1, lvl + 1) - instr(STR, '/', 1, lvl) - 1) name
FROM
  ( SELECT '/' || STR || '/' AS STR, id FROM TAB
  ),
  ( SELECT level AS lvl FROM dual CONNECT BY level <= 100
  )
WHERE lvl <= LENGTH(STR) - LENGTH(REPLACE(STR, '/')) - 1
ORDER BY ID,
  NAME
  )
  LOOP
    --IF i.ID = 1001 THEN
    l_res_coll.extend;
    l_index            := l_res_coll.count;
    l_res_coll(l_index):= T_TABLE(i.ID, i.name);
    --END IF;
  END LOOP;
RETURN l_res_coll;
END;

--------------------------------------------------------
--  DDL for FUNCTION F_GET_PRIVACY_MEETING
--------------------------------------------------------
create or replace FUNCTION F_GET_PRIVACY_MEETING(
    in_user_id IN NUMBER, meeting_id IN NUMBER)
  RETURN VARCHAR2
IS
  tmp NVARCHAR2 (4000);
  CURSOR user_list
  IS
    SELECT
      CASE
        WHEN m.PRIVACY = 1
        THEN
          (SELECT
            CASE
              WHEN COUNT(*) > 0
              THEN 'true'
              ELSE 'false'
            END
          FROM MEETING_MEMBER mm
          WHERE mm.MEETING_ID = m.ID
          AND (m.CREATED_BY   = in_user_id
          OR (mm.MEMBER_ID    = in_user_id
          AND mm.TYPE         = 0)
          OR (mm.ORG_ID      IN
            (SELECT u.SYS_ORGANIZATION_ID
            FROM user_role u
            JOIN sys_role sr
            ON u.SYS_ROLE_ID    = sr.SYS_ROLE_ID
            WHERE u.SYS_USER_ID = in_user_id
            AND sr.CODE        IN ('TTDV', 'LDDV', 'QLLH')
            )
        AND mm.TYPE             = 1)
          OR m.ORG_APPROVAL_ID IN
            (SELECT u.SYS_ORGANIZATION_ID
            FROM user_role u
            JOIN sys_role sr
            ON u.SYS_ROLE_ID    = sr.SYS_ROLE_ID
            WHERE u.SYS_USER_ID = in_user_id
            AND sr.CODE        IN ('QLLH')
            )
          OR (mm.MEMBER_ID IN
            (SELECT mas.LEADER_ID
            FROM MEETING_ASSISTANT mas
            WHERE mas.STATUS    = 1
            AND ( mas.ASSI_TYPE = 1
            OR mas.ASSI_TYPE    = 5
            OR mas.ASSI_TYPE    = 6)
            AND mas.DEL_FLAG    = 0
            AND mas.EMPLOYEE_ID = in_user_id
            )
          AND mm.TYPE       = 0
          AND mm.MEMBER_ID IN
            (SELECT ur.SYS_USER_ID
            FROM USER_ROLE ur
            JOIN sys_role sr
            ON ur.SYS_ROLE_ID = sr.SYS_ROLE_ID
            WHERE sr.CODE    IN ('TTDV', 'LDDV')
            )))
          )
        ELSE 'true'
      END AS checkPrivacy
    FROM meeting m
    WHERE m.DEL_FLAG = 0
    AND m.id         = meeting_id;
  BEGIN
    FOR user_id IN user_list
    LOOP
      tmp := user_id.checkPrivacy;
    END LOOP;
    RETURN tmp;
  END;