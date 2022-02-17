CREATE TABLE `customer`
(
    `id`        mediumint(8) unsigned NOT NULL auto_increment,
    `first_name` varchar(255) default NULL,
    `last_name`  varchar(255) default NULL,
    `birthdate` varchar(255),
    PRIMARY KEY (`id`)
) AUTO_INCREMENT = 1;