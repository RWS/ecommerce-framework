Hybris Connector
=================

The Hybris connector implements the following E-Commerce Framework capabilities:

* Product Search
* Product Categories
* Facets
* Product Lister
* Breadcrumb
* Search Spell Feedback
* Product Detail
* Cart

The connector has been verified against OCC v1 (Omni Commerce Connect) on hybris Commerce Suite 5.x

Note: This connector is deprecated in favour of the new Hybris connector for the Tridion Integration Framework.

Configuration
---------------

To configure the connector you need to have the following properties in your Spring config or external application config.
Example of a application.properties file:

```
# Hybris Application Configuration
#

hybris.url = [Hybris URL,e.g. http://localhost:9001/rest/v1/]
hybris.username = [Username]
hybris.password = [Password]
hybris.categoryExpiryTimeout = 6000000
hybris.mediaUrlPrefix = [Media URL, e.g. http://localhost:9001]
```