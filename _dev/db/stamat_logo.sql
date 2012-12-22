-- phpMyAdmin SQL Dump
-- version 3.3.9.2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Nov 13, 2012 at 11:33 AM
-- Server version: 5.5.9
-- PHP Version: 5.3.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `logoDetection`
--

-- --------------------------------------------------------

--
-- Table structure for table `logo`
--

CREATE TABLE `logo` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `url` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Dumping data for table `logo`
--


-- --------------------------------------------------------

--
-- Table structure for table `logsLogo`
--

CREATE TABLE `logsLogo` (
  `id_logs` int(11) NOT NULL AUTO_INCREMENT,
  `type` varchar(200) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `text` text,
  PRIMARY KEY (`id_logs`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Dumping data for table `logsLogo`
--


-- --------------------------------------------------------

--
-- Table structure for table `process`
--

CREATE TABLE `process` (
  `idProcessNum` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `idProcessStatus` int(11) DEFAULT NULL,
  `id_logo` mediumint(8) unsigned NOT NULL,
  `id_video` mediumint(8) unsigned NOT NULL,
  `start` timestamp NULL DEFAULT NULL,
  `end` timestamp NULL DEFAULT NULL,
  `OSProcessId` varchar(255) DEFAULT NULL,
  `command` text,
  `stringStatus` varchar(100) DEFAULT NULL,
  `estimatedEnd` time DEFAULT NULL,
  `priority` int(11) NOT NULL DEFAULT '1',
  `output` text,
  `detection` mediumint(8) unsigned NOT NULL,
  PRIMARY KEY (`idProcessNum`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Dumping data for table `process`
--


-- --------------------------------------------------------

--
-- Table structure for table `processstatus`
--

CREATE TABLE `processstatus` (
  `idProcessStatus` int(11) NOT NULL AUTO_INCREMENT,
  `order` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`idProcessStatus`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Dumping data for table `processstatus`
--


-- --------------------------------------------------------

--
-- Table structure for table `video`
--

CREATE TABLE `video` (
  `id` mediumint(9) NOT NULL AUTO_INCREMENT,
  `url` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Dumping data for table `video`
--

