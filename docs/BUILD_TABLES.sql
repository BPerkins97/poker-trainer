CREATE TABLE INFOSET(
    INFOSET_ID INT AUTO_INCREMENT PRIMARY KEY,
    BETTING_ROUND INT NOT NULL,
    PLAYER INT NOT NULL,
    CARDS VARCHAR(14),
    HISTORY VARCHAR(255)
);

CREATE TABLE NODES(
    INFOSET_ID INT,
    ACTION_ID VARCHAR(4),
    REGRET INT NOT NULL DEFAULT 0,
    AVERAGE_ACTION INT NOT NULL DEFAULT 0,
    PRIMARY KEY (INFOSET_ID, ACTION_ID),
    FOREIGN KEY (INFOSET_ID) REFERENCES INFOSET(INFOSET_ID)
);

INSERT INTO INFOSET SET BETTING_ROUND = ?, PLAYER = ?, CARDS = ?, HISTORY = ?