CREATE TABLE `question` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `question_url` varchar(255) DEFAULT NULL,
  `true_answer` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `facebook_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `my_index` (`id`,`facebook_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `competition_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `competition_status` int(11) DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `my_index` (`competition_status`,`user_id`,`id`),
  KEY `FK7kciowsl4q3qpqicglqxio2yt` (`user_id`),
  CONSTRAINT `FK7kciowsl4q3qpqicglqxio2yt` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `question_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `answer_time` datetime DEFAULT NULL,
  `question_number` int(11) DEFAULT NULL,
  `question_status` int(11) DEFAULT NULL,
  `user_answer` varchar(255) DEFAULT NULL,
  `competition_log_id` int(11) DEFAULT NULL,
  `question_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `my_index` (`user_id`,`question_id`,`competition_log_id`,`user_answer`,`question_status`),
  KEY `FKdpv3tnhoftavo9f0ydpu2dkcn` (`competition_log_id`),
  KEY `FKqwkxpdl3ma2mbusyyve7njd9u` (`question_id`),
  CONSTRAINT `FK7lwnl8xwx5ms72dho865fadfn` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKdpv3tnhoftavo9f0ydpu2dkcn` FOREIGN KEY (`competition_log_id`) REFERENCES `competition_log` (`id`),
  CONSTRAINT `FKqwkxpdl3ma2mbusyyve7njd9u` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;




