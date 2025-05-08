INSERT INTO global_settings (setting_key, setting_value) VALUES

                                                             ('maintenance_mode', 'false'),
                                                             ('contact_support_email', 'support@bank.com'),

                                                             ('duree_session_admin', '60'),
                                                             ('duree_session_agent', '45'),
                                                             ('duree_session_client', '15'),

                                                             ('plafond_virement', '10000'),
                                                             ('frais_virement_pourcentage', '2.5'),
                                                             ('plafond_recharge', '200'),
                                                             ('delai_validation_virement', '48'),
                                                             ('plafond_retrait', '5000'),
                                                             ('plafond_achat_en_ligne', '3000'),

                                                             ('expiry_date', '2025-12-31'), -- exemple de date ISO
                                                             ('nb_max_tentatives_connexion', '5'),
                                                             ('delai_blocage', '30'),

                                                             ('niveau_journalisation', 'INFO'),
                                                             ('activer_audit_transactions', 'true');
