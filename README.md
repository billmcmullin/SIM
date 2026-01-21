# SIM - Python App for getting Chat Widgets Messages

This is python CLI that will make request to chat widgets and perform the following:

* Retrieve the chats performed from the Embedded Chat widgets
* Determine if they are coming from the Website, Portal, Forum
* Separate each chat widget responses into their own: CSV file, Text file, JSON file
* Able to determine what topic was being asked in the prompt

## How to use

Run the following to perform function all functions:

```
python main.py --export-and-search --embed-map <IDS_JSON_FILE> --api-key <API KEY> --base "<URL>" -- --per-widget --csv csv/output.csv --details-dir csv --terms-file <TERMS TEXT FILE>
```

Run to perform just export function:

```
python main.py --embed-map <IDS_JSON_FILE> --api-key <API KEY> --base "<URL>"
```

Run to perform just search function with options:

```
python main.py --search --per-widget --csv csv/output.csv --details-dir csv --terms-file <TERMS TEXT FILE>
```

## Example Files to use

Example terms.txt:

```
Term1
Term2
Term3=(?i)(?<!\w)(?:TERM3|Term3|TeRM3)(?!\w)
```

Example widgets_id.json:

```
{
    "<WIDGET ID>": "<NAME OF WIDGET>"
}
```
