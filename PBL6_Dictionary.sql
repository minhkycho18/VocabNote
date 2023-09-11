CREATE TABLE vocabularies(
   vocab_id BIGINT AUTO_INCREMENT PRIMARY KEY,
   word VARCHAR(100) NOT NULL,
   pos      VARCHAR(100) NOT NULL,
   phonetic VARCHAR(100),
   audio_url TEXT,
   CONSTRAINT head_pos UNIQUE(word, pos)
);

CREATE TABLE definitions(
	def_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word_desc TEXT NOT NULL,
    examples TEXT
);

CREATE TABLE vocab_def(
    vocab_id BIGINT,
    def_id BIGINT,
    PRIMARY KEY (vocab_id, def_id),
    FOREIGN KEY (vocab_id) REFERENCES vocabularies(vocab_id),
    FOREIGN KEY (def_id) REFERENCES definitions(def_id)
);


