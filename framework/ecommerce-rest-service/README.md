  E-Commerce Service
========================
  
# REST

  
## Description

#### Category
URL: http://localhost:8097/ecommerce.svc/rest/v1/{locale}/category/{id or path}

#### Product
URL: http://localhost:8097/ecommerce.svc/rest/v1/{locale}/product/{id or variant}
  
#### Cart
URL: http://localhost:8097/ecommerce.svc/rest/v1/{locale}/cart/....
  
## Example REST Requests


### Category

http://localhost:8097/ecommerce.svc/rest/v1/category/catalog01_18661_17627


### Product

http://localhost:8097/ecommerce.svc/rest/v1/product/008010111647
http://localhost:8097/ecommerce.svc/rest/v1/product/008010111647?color=1993

### Product Query

http://localhost:8097/ecommerce.svc/rest/v1/en_GB/product/query?categoryId=catalog01_18661

### Search

http://localhost:8097/ecommerce.svc/rest/v1/en_GB/product/query?searchPhrase?red
http://localhost:8097/ecommerce.svc/rest/v1/en_GB/product/query?searchPhrase=women+watch&facets='brand=dkny'
http://localhost:8097/ecommerce.svc/rest/v1/en_GB/product/query?facets='spotlight=new&brand=adidas&price=10.0-20.0'


### Cart

#### Create cart
http://localhost:8097/ecommerce.svc/rest/v1/cart (POST)

#### Update cart
http://localhost:8097/ecommerce.svc/rest/v1/cart/{cartId} (POST)

Body:

```
[
    {
       "itemId": "061010828997",
       "operation": "add",
       "quantity": 1
    },
    {
       "itemId": "061010828998",
       "operation": "remove"
    }
    ...
]
```


# Graph-QL

## Description

URL: http://localhost:8097/ecommerce.svc/{locale}/v1/graphql 

## Example Graph-QL Requests





