create table "users"
(
  "id" serial NOT NULL,
  "handle" character varying NOT NULL,
  "display_name" character varying NOT NULL,
  "email_address" character varying,
  "password" character varying(128),
  "salt" character varying(10),
  "verified" boolean default false NOT NULL,
  "active" boolean default false NOT NULL,
  "token" character varying(32),
  CONSTRAINT "users_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "users_u_handle" UNIQUE ("handle"),
  CONSTRAINT "users_u_email" UNIQUE ("email_address")
)
