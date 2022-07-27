USE KUHN;
SET SESSION cte_max_recursion_depth = 100;

CREATE TABLE NODE(HISTORY VARCHAR(100), ACTION char(1), REGRET int NOT NULL DEFAULT 0, PRIMARY KEY(HISTORY, ACTION));
DROP TABLE NODE;

select * from NODE;

INSERT INTO NODE
WITH RECURSIVE CARDS (CARD) AS (
    VALUES ROW(0), ROW(1), ROW(2)
),
               CARD_INFOSETS (PLAYER_1_CARD, PLAYER_2_CARD) AS (
                   SELECT c1.CARD, c2.CARD
                   FROM CARDS c1
                            CROSS JOIN CARDS c2 on c2.card <> c1.card
               ),
               ACTIONS (ACTION) AS (
                   VALUES ROW('p'), ROW('b')
               ),
               HISTORIES (HISTORY) AS (
                   SELECT CAST('' AS CHAR(100))
                   UNION ALL
                   SELECT CAST(CONCAT(h.HISTORY, a.ACTION) AS CHAR(100))
                   FROM HISTORIES h
                            JOIN ACTIONS a ON right(concat(h.history, a.ACTION), 2) <> 'bb' AND right(concat(h.history, a.ACTION), 2) <> 'pp' AND right(concat(h.HISTORY, a.ACTION), 2) <> 'bp'
    ),
    NODES AS (
select CONCAT(c.card, h.history) AS HISTORY, a.ACTION, 0 AS REGRET
from histories h
    cross join CARDS c
    cross join ACTIONS a
    )
SELECT * FROM NODES;