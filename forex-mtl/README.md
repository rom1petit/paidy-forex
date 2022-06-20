
- The service returns an exchange rate when provided with 2 supported currencies
- The rate should not be older than 5 minutes
- The service should support at least 10,000 successful requests per day with 1 API token
  - 9 currencies are supported: for each currency 9 currencies x 8 pairs x 2 direction (e.g. JPY/AUD, AUD/JPY) = 77 x 2 = 144 possible pairs
  - refresh rate for all pair cache every 2 minutes => 740 request / day 
test one frame:
```
curl -H "token: 10dc303535874aeccc86a8251e6992f5" "localhost:8080/rates?from=USD&to=JPY"
```

test forex
```
curl -H "Authorization: Bearer f294a63479ceac267bad48596e450447217cd2a354d7ba8aa906631f8067d8bc" "localhost:9080/v1/rate?from=USD&to=JPY"
```
