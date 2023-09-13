CREATE TABLE vocabularies(
   vocab_id BIGINT AUTO_INCREMENT PRIMARY KEY,
   word VARCHAR(100) COLLATE utf8mb4_0900_as_cs NOT NULL,
   pos      VARCHAR(100) NOT NULL,
   phonetic VARCHAR(100),
   audio_url TEXT,
   CONSTRAINT head_pos UNIQUE(word, pos)
);

CREATE TABLE definitions(
	def_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word_desc LONGTEXT NOT NULL,
    examples LONGTEXT
);

CREATE TABLE vocab_def(
    vocab_id BIGINT,
    def_id BIGINT,
    PRIMARY KEY (vocab_id, def_id),
    FOREIGN KEY (vocab_id) REFERENCES vocabularies(vocab_id),
    FOREIGN KEY (def_id) REFERENCES definitions(def_id)
);


