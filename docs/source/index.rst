.. flow documentation master file, created by
   sphinx-quickstart on Thu May  3 10:29:25 2018.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

QPointz
==================================
* :ref:`delta_toc`
* :ref:`rapids_toc`

.. _delta_toc:
.. toctree::    
   :titlesonly: 
   :maxdepth: 2
   :caption: Delta services
   :glob:

   Types system <delta/typessystem>


.. _rapids_toc:
.. toctree::    
   :titlesonly: 
   :maxdepth: 2
   :caption: Rapids services
   :glob:

   Quick Start <rapids/quickstart>
   Services <rapids/services>
   Deployments <rapids/deployment>
   Configuration <rapids/configuration>


Legacy
======


* :ref:`about_toc`
* :ref:`overview_toc`
* :ref:`filesystems_toc`
* :ref:`formats_toc`
* :ref:`install_toc`
* :ref:`schema_toc`


.. _about_toc:
.. toctree::    
   :titlesonly: 
   :maxdepth: 2
   :caption: About

   about/Overview

.. _overview_toc:
.. toctree::    
   :titlesonly: 
   :maxdepth: 3
   :caption: Overview
   :glob:

   overview/*
   

.. _filesystems_toc:
.. toctree::
   :maxdepth: 2
   :caption: File systems

   fs/*

.. _formats_toc:
.. toctree::
   :maxdepth: 2
   :caption: Formats
   :glob:

   formats/Overview
   formats/*


.. _install_toc:
.. toctree::    
   :titlesonly: 
   :maxdepth: 2    
   :caption: Installation   

   install/Overview
   install/RunBehindReverseProxy

.. _schema_toc:
.. toctree::        
    :maxdepth: 2        
    :caption: Metadata schema
    
    schema/Structure
    schema/Overview     
    schema/Feed
    schema/Interface