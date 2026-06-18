-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 17-05-2026 a las 21:27:23
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `househunter`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `datos_empresas`
--

CREATE TABLE `datos_empresas` (
  `id_usuario` int(11) NOT NULL,
  `cuit` varchar(20) NOT NULL,
  `razon_social` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `datos_empresas`
--

INSERT INTO `datos_empresas` (`id_usuario`, `cuit`, `razon_social`) VALUES
(2, '30-12345678-9', 'Globant S.A.');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `datos_personas`
--

CREATE TABLE `datos_personas` (
  `id_usuario` int(11) NOT NULL,
  `dni` varchar(15) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `datos_personas`
--

INSERT INTO `datos_personas` (`id_usuario`, `dni`, `nombre`, `apellido`) VALUES
(1, '47299224', 'Augusto', 'Karacsany'),
(3, '44555666', 'Luca', 'Borrelli'),
(4, '99999999', 'Franco', 'Colapinto');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuarios`
--

CREATE TABLE `usuarios` (
  `id` int(11) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `rol` enum('ADMINISTRADOR','EMPRESA','INVITADO') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuarios`
--

INSERT INTO `usuarios` (`id`, `email`, `password`, `rol`) VALUES
(1, 'admin@hotel.com', '$2a$10$uF/66Rb9xmS8jD3cSVtP.eXIaRujgfBi9KD8e68y9Zd3NcgD6i83i', 'ADMINISTRADOR'),
(2, 'info@globant.com', '$2a$10$wjR9D2Q5HYWx41yfG89LE.PQDi.FF7WE7YNxHw4IMbxD9rH12MurC', 'EMPRESA'),
(3, 'invitado@gmail.com', '$2a$10$q/ABRKqFBznwHcrPci3st.rAjlfxQkZMzrAHGXmSi7zjPMFkUHCo6', 'INVITADO'),
(4, 'invitado1@gmail.com', '$2a$10$tHd/HAtSKXNyhggL5TYfYe5HcYWe1gA04cLpY0OJG2FIYxMz7u2bm', 'INVITADO');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `datos_empresas`
--
ALTER TABLE `datos_empresas`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `cuit` (`cuit`);

--
-- Indices de la tabla `datos_personas`
--
ALTER TABLE `datos_personas`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `dni` (`dni`);

--
-- Indices de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `datos_empresas`
--
ALTER TABLE `datos_empresas`
  ADD CONSTRAINT `datos_empresas_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;

--
-- Filtros para la tabla `datos_personas`
--
ALTER TABLE `datos_personas`
  ADD CONSTRAINT `datos_personas_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
