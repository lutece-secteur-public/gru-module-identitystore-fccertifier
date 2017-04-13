INSERT INTO identitystore_client_application( id_client_app , name , code )
VALUES ( 2, 'FranceConnectCertifier' , 'FranceConnectCertifier' );

INSERT INTO identitystore_attribute_right ( id_client_app , id_attribute , readable , writable , certifiable ) VALUES
( 2 , 1 , 1 , 0 , 1 ),
( 2 , 3 , 1 , 0 , 1 ),
( 2 , 4 , 1 , 0 , 1 ),
( 2 , 11 , 1 , 0 , 1 ),
( 2 , 19 , 1 , 0 , 1 ),
( 2 , 30 , 0 , 0 , 1 ),
( 2 , 31 , 0 , 0 , 1 ),
( 2 , 32 , 0 , 0 , 1 ),
( 2 , 33 , 0 , 0 , 1 ),
( 2 , 34 , 0 , 0 , 1 ),
( 2 , 35 , 0 , 0 , 1 );